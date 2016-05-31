/*
 * Copyright (c) 2015 - 2016 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.opendaylight.nic.of.renderer.api.Observer;
import org.opendaylight.nic.of.renderer.api.Subject;
import org.opendaylight.nic.of.renderer.strategy.DefaultExecutor;
import org.opendaylight.nic.of.renderer.strategy.MPLSExecutor;
import org.opendaylight.nic.of.renderer.strategy.QoSExecutor;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.nic.utils.exceptions.IntentElementNotFoundException;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Redirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.QosConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by saket on 8/19/15.
 */
public class OFRendererFlowManagerProvider implements OFRendererFlowService, Observer, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(OFRendererFlowManagerProvider.class);
    private Set<ServiceRegistration<?>> serviceRegistration;
    private IntentFlowManager intentFlowManager;
    private ArpFlowManager arpFlowManager;
    private LldpFlowManager lldpFlowManager;
    private IntentMappingService intentMappingService;
    private DataBroker dataBroker;
    private final PipelineManager pipelineManager;
    private OFRendererGraphService graphService;
    private MplsIntentFlowManager mplsIntentFlowManager;
    private QosConstraintManager qosConstraintManager;
    private Registration pktInRegistration;
    private RedirectFlowManager redirectFlowManager;
    private Subject topic;

    private NotificationProviderService notificationProviderService;

    public OFRendererFlowManagerProvider(DataBroker dataBroker,
                                         PipelineManager pipelineManager,
                                         IntentMappingService intentMappingService,
                                         NotificationProviderService notificationProviderService) {
        this.dataBroker = dataBroker;
        this.pipelineManager = pipelineManager;
        this.serviceRegistration = new HashSet<ServiceRegistration<?>>();
        this.intentMappingService = intentMappingService;
        this.notificationProviderService = notificationProviderService;
    }

    public void init() {
        LOG.info("OF Renderer Provider Session Initiated");
        // Register this service with karaf
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        graphService = new NetworkGraphManager();
        graphService.register(this);
        mplsIntentFlowManager = new MplsIntentFlowManager(dataBroker, pipelineManager);
        serviceRegistration.add(context.registerService(OFRendererFlowService.class, this, null));
        serviceRegistration.add(context.registerService(OFRendererGraphService.class, graphService, null));
        intentFlowManager = new IntentFlowManager(dataBroker, pipelineManager);
        arpFlowManager = new ArpFlowManager(dataBroker, pipelineManager);
        lldpFlowManager = new LldpFlowManager(dataBroker, pipelineManager);
        qosConstraintManager = new QosConstraintManager(dataBroker, pipelineManager);
        this.redirectFlowManager = new RedirectFlowManager(dataBroker, pipelineManager, graphService);
        this.pktInRegistration = notificationProviderService.registerNotificationListener(redirectFlowManager);
    }

    @Override
    public void pushIntentFlow(Intent intent, FlowAction flowAction) {
        // TODO: Extend to support other actions
        LOG.info("Intent: {}, FlowAction: {}", intent.toString(), flowAction.getValue());

        // Creates QoS configuration and stores profile in the Data Store.
        if (intent.getQosConfig() != null) {
            return;
        }
        //TODO: Change to use Command Pattern
        try {
            if (isMPLS(intent)) {
                new MPLSExecutor(mplsIntentFlowManager, intentMappingService, graphService).execute(intent, flowAction);
            } else if (isQoS(intent)) {
                new QoSExecutor(qosConstraintManager, dataBroker).execute(intent, flowAction);
            } else if (isRedirect(intent)) {
                redirectFlowManager.redirectFlowConstruction(intent, flowAction);
            } else {
                new DefaultExecutor(intentFlowManager, dataBroker).execute(intent, flowAction);
            }
        } catch (IntentInvalidException ie) {
            //TODO: Implement an action for Exception cases
        }
    }

    protected boolean isRedirect(Intent intent) {
        Action actionContainer = IntentUtils.getAction(intent);
        return (Redirect.class.isInstance(actionContainer));
    }

    protected boolean isMPLS(Intent intent) throws IntentInvalidException {
        EndPointGroup source = IntentUtils.extractSrcEndPointGroup(intent);
        EndPointGroup target = IntentUtils.extractDstEndPointGroup(intent);
        Map<String, String> sourceContent = getMappingServiceContent(source);
        Map<String, String> targetContent = getMappingServiceContent(target);
        return (sourceContent.containsKey(OFRendererConstants.MPLS_LABEL_KEY)
                && targetContent.containsKey(OFRendererConstants.MPLS_LABEL_KEY));
    }

    private boolean isQoS(Intent intent) {
        final Action actionContainer = IntentUtils.getAction(intent);
        final List<String> endPointGroups = IntentUtils.extractEndPointGroup(intent);
        return (checkQosConstraint(intent, actionContainer, endPointGroups));
    }

    //FIXME move to a utility class
    @Override
    public void pushARPFlow(NodeId nodeId, FlowAction flowAction) {
        arpFlowManager.pushFlow(nodeId, flowAction);
    }

    @Override
    public void close() throws Exception {
        if (redirectFlowManager != null) {
            redirectFlowManager.close();
        }
        if (pktInRegistration != null) {
            pktInRegistration.close();
        }
        for (ServiceRegistration<?> service: serviceRegistration) {
            if (service != null) {
                service.unregister();
            }
        }
    }

    /**
     * Push a LLDP flow onto an Inventory {@link NodeId} so that
     * OpenDaylight can know how the devices are connected to each others.
     * This function is necessary for OF protocols above 1.0
     * @param nodeId The Inventory {@link NodeId}
     * @param flowAction The {@link FlowAction} to push
     */
    @Override
    public void pushLLDPFlow(NodeId nodeId, FlowAction flowAction) {
        lldpFlowManager.pushFlow(nodeId, flowAction);
    }

    /**
     * Checks the Constraint name is present in the constraint container.
     * @param intent  Intent
     * @param actionContainer Action
     * @param endPointGroups List of Endpoints
     * @return boolean
     */
    private boolean checkQosConstraint(Intent intent, Action actionContainer, List<String> endPointGroups) {
        //Check for constrain name in the intent.
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.Constraints constraintContainer
                    = intent.getConstraints().get(0).getConstraints();
        if (!constraintContainer.getImplementedInterface().isAssignableFrom(QosConstraint.class)) {
            return false;
        }
        String qosName = ((QosConstraint)constraintContainer).getQosConstraint().getQosName();
        LOG.info("QosConstraint is set to: {}", qosName);
        if (qosName != null) {
            //Set the values to QosConstraintManager
            qosConstraintManager.setQosName(qosName);
            qosConstraintManager.setEndPointGroups(endPointGroups);
            qosConstraintManager.setAction(actionContainer);
            qosConstraintManager.setConstraint(constraintContainer);
        } else {
            LOG.trace("QoS Name is not set");
            return false;
        }
        return true;
    }

    @Override
    public void update() {
        Intent msg = (Intent) topic.getUpdate(this);
        if (msg != null) {
            pushIntentFlow(msg, FlowAction.ADD_FLOW);
        }
    }

    @Override
    public void setSubject(Subject sub) {
        this.topic = sub;
    }

    private Map<String, String> getMappingServiceContent(EndPointGroup endPointGroup)
            throws IntentElementNotFoundException {
        String endPointGroupName = endPointGroup.getEndPointGroup().getName();
        final String CONTENT_NOT_FOUND_MESSAGE = "Content not found for EndPointGroup: " + endPointGroupName;
        final Map<String, String> contentMap = intentMappingService.get(endPointGroupName);
        if(contentMap.isEmpty()) {
            throw new IntentElementNotFoundException(CONTENT_NOT_FOUND_MESSAGE);
        }
        return contentMap;
    }
}
