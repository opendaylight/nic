/*
 * Copyright (c) 2015 - 2016 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.opendaylight.nic.of.renderer.api.Observer;
import org.opendaylight.nic.of.renderer.api.Subject;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.constraints.rev150122.FailoverType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Redirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.FailoverConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.ProtectionConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.QosConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.gson.Gson;

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

        Action actionContainer = intent.getActions().get(0).getAction();

        List<String> endPointGroups = IntentUtils.extractEndPointGroup(intent);
        // MPLS stuff
        String sourceIntent = endPointGroups.get(OFRendererConstants.SRC_END_POINT_GROUP_INDEX);
        String targetIntent = endPointGroups.get(OFRendererConstants.DST_END_POINT_GROUP_INDEX);
        Map<String, String> sourceContent = intentMappingService.get(sourceIntent);
        Map<String, String> targetContent = intentMappingService.get(targetIntent);
        if (sourceContent.containsKey(OFRendererConstants.MPLS_LABEL_KEY)
                && targetContent.containsKey(OFRendererConstants.MPLS_LABEL_KEY)) {
            mplsIntentFlowManager.setEndPointGroups(endPointGroups);
            mplsIntentFlowManager.setAction(actionContainer);
            mplsIntentFlowManager.setSubjectsMapping(extractSubjectDetails(endPointGroups));

            // Look for protection and failover constraints for intent
            boolean isProtected = false;
            FailoverType failoverType = null;
            if (intent.getConstraints() != null) {
                for (Constraints constraints : intent.getConstraints()) {
                    if (constraints.getConstraints() instanceof ProtectionConstraint) {
                        ProtectionConstraint protectionConstraint = (ProtectionConstraint) constraints.getConstraints();
                        isProtected = protectionConstraint.getProtectionConstraint().isIsProtected();
                        LOG.trace("Protection is set to: {}", isProtected);
                    } else if (constraints.getConstraints() instanceof FailoverConstraint) {
                        FailoverConstraint failoverConstraint = (FailoverConstraint) constraints.getConstraints();
                        failoverType = failoverConstraint.getFailoverConstraint().getFailoverSelector();
                        LOG.trace("failoverType is set to: {}", failoverType);
                    }
                }
            }
            generateMplsFlows(intent, sourceIntent, targetIntent, failoverType, flowAction, isProtected);
        } else if (checkQosConstraint(intent, actionContainer, endPointGroups)) {
            //Get all node Id's
            Map<Node, List<NodeConnector>> nodeMap = getNodes();
            for (Map.Entry<Node, List<NodeConnector>> entry : nodeMap.entrySet()) {
                //Push flow to every node for now
                qosConstraintManager.pushFlow(entry.getKey().getId(), flowAction);
            }
        } else if (actionContainer instanceof Redirect) {
            redirectFlowManager.redirectFlowConstruction(intent, flowAction);
        } else {
            intentFlowManager.setEndPointGroups(endPointGroups);
            intentFlowManager.setAction(actionContainer);
            intentFlowManager.setIntent(intent);
            //Get all node Id's
            Map<Node, List<NodeConnector>> nodeMap = getNodes();
            for (Map.Entry<Node, List<NodeConnector>> entry : nodeMap.entrySet()) {
                //Push flow to every node for now
                intentFlowManager.pushFlow(entry.getKey().getId(), flowAction);
            }
        }
    }

    /**
     * Calls the appropriate functions to generate a shortest path
     * route between a source and a target.
     * @param source The Source {@link Subjects}
     * @param target The target {@link Subjects}
     * @param flowAction
     */
    private void generateMplsFlows(Intent intent, String source, String target, FailoverType failoverType,
            FlowAction flowAction, boolean isProtected) {
        LOG.info("Generating Intent Flow from {} to {}", source, target);
        String sourceNodeConnectorId = intentMappingService.get(source)
                                                           .get(OFRendererConstants.SWITCH_PORT_KEY);
        String targetNodeConnectorId = intentMappingService.get(target)
                                                           .get(OFRendererConstants.SWITCH_PORT_KEY);
        LOG.info("Source port: {} Target port: {}", sourceNodeConnectorId, targetNodeConnectorId);
        org.opendaylight.yang.gen.v1.urn
            .tbd.params.xml.ns.yang.network
            .topology.rev131021.NodeId sourceNodeId = extractTopologyNodeId(sourceNodeConnectorId);
        org.opendaylight.yang.gen.v1.urn
            .tbd.params.xml.ns.yang.network
            .topology.rev131021.NodeId targetNodeId = extractTopologyNodeId(targetNodeConnectorId);
        List<Link> paths = null;
        if (isProtected && failoverType != null && failoverType == FailoverType.SlowReroute) {
            if (flowAction.equals(FlowAction.ADD_FLOW)) {
                NetworkGraphManager.ProtectedLinks.put(intent,
                        graphService.getDisjointPaths(sourceNodeId, targetNodeId));
                paths = NetworkGraphManager.ProtectedLinks.get(intent).get(0);
            } else if (flowAction.equals(FlowAction.REMOVE_FLOW)) {
                paths = NetworkGraphManager.ProtectedLinks.get(intent).get(0);
                NetworkGraphManager.ProtectedLinks.remove(intent);
            }
        } else {
            LOG.info("Intent has no constraints for protection");
            paths = graphService.getShortestPath(sourceNodeId, targetNodeId);
        }

        LOG.trace("MPLS Source NodeId {}", sourceNodeId.getValue());
        LOG.trace("MPLS Target NodeId {}", targetNodeId.getValue());
        LOG.info("Retrieved shortest path, there are {} hops.", paths.size());
        if (paths != null && !paths.isEmpty()) {
            LOG.info("Retrieved shortest path, there are {} hops.", paths.size());
            for (Link link : paths) {
                NodeId linkSourceNodeId = new NodeId(link.getSource().getSourceNode().getValue());
                NodeId linkTargetNodeId = new NodeId(link.getDestination().getDestNode().getValue());
                String linkSourceTp = link.getSource().getSourceTp().getValue();
                // Shortest path is giving result from end to beginning
                if (sourceNodeId.getValue().equals(linkSourceNodeId.getValue())) {
                    mplsIntentFlowManager.pushMplsFlow(linkSourceNodeId, flowAction, linkSourceTp);
                } else if(linkTargetNodeId.getValue().equals(targetNodeId.getValue())) {
                    mplsIntentFlowManager.forwardMplsFlow(linkSourceNodeId, flowAction, linkSourceTp);
                    mplsIntentFlowManager.popMplsFlow(linkTargetNodeId, flowAction, targetNodeConnectorId);
                } else {
                    mplsIntentFlowManager.forwardMplsFlow(linkSourceNodeId, flowAction, linkSourceTp);
                }
            }
        }
    }

    //FIXME move to a utility class
    /**
     * Derivate a {@link org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId}
     * from a {@link NodeConnector}.
     * @param nodeConnectorId The Inventory NodeConnectorId
     * @return NodeId contained in Network-Topology
     */
    private org.opendaylight.yang.gen.v1.urn
                .tbd.params.xml.ns.yang.network
                .topology.rev131021.NodeId extractTopologyNodeId(String nodeConnectorId) {
        List<String> split = Arrays.asList(nodeConnectorId.split(":"));
        return new org.opendaylight.yang.gen.v1.urn
                       .tbd.params.xml.ns.yang.network
                       .topology.rev131021.NodeId(split.get(0) +
                               ":" +
                               split.get(1));
    }

    @Override
    public void pushARPFlow(NodeId nodeId, FlowAction flowAction) {
        arpFlowManager.pushFlow(nodeId, flowAction);
    }

    /**
     * Retrieve all the {@link Node} along with a list of their
     * associated {@link NodeConnector}.
     * @return nodes A map with {@link Node} as the key and a {@link List} of {@link NodeConnector}.
     */
    private Map<Node, List<NodeConnector>> getNodes() {
        Map<Node, List<NodeConnector>> nodeMap = new HashMap<Node, List<NodeConnector>>();
        Nodes nodeList = new NodesBuilder().build();
        ReadTransaction tx = dataBroker.newReadOnlyTransaction();
        try {
            final InstanceIdentifier<Nodes> nodesIdentifier = InstanceIdentifier.create(Nodes.class);
            final CheckedFuture<Optional<Nodes>, ReadFailedException> txCheckedFuture = tx.read(LogicalDatastoreType
                    .OPERATIONAL, nodesIdentifier);
            nodeList = txCheckedFuture.checkedGet().get();

            for (Node node : nodeList.getNode()) {
                LOG.info("Node ID : {}", node.getId());
                List<NodeConnector> nodeConnector = node.getNodeConnector();
                nodeMap.put(node, nodeConnector);
            }
        } catch (ReadFailedException e) {
            //TODO: Perform fail over
            LOG.error("Error reading Nodes from MD-SAL", e);
        }
        return nodeMap;
    }

    /**
     * Creates a hashmap of the mapping information map for every subject
     * @param endPointGroups list of endpoint group from Intent request
     * @return nested hashmap with mapping details of subjects
     */
    private Map<String, Map<String, String>> extractSubjectDetails(List<String> endPointGroups) {
        Map<String, Map<String, String>> subjectsMapping = new HashMap<String, Map<String, String>>();
        for (String id : endPointGroups) {
            Map<String, String> values = intentMappingService.get(id);
            if( values != null && values.size() >0 ) {
                subjectsMapping.put(id, values);
            } else {
                LOG.warn("No key found for {} in IntentMappingService", id);
            }
        }
        return subjectsMapping;
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

    @Override
    /**
     * Push a LLDP flow onto an Inventory {@link NodeId} so that
     * OpenDaylight can know how the devices are connected to each others.
     * This function is necessary for OF protocols above 1.0
     * @param NodeId The Inventory {@link NodeId}
     * @param action The {@link FlowAction} to push
     */
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

	@Override
	public String getShortestPath(String srcIP, String dstIP) {
		String sourceNodeConnectorId = intentMappingService.get(srcIP)
                .get(OFRendererConstants.SWITCH_PORT_KEY);
		String targetNodeConnectorId = intentMappingService.get(dstIP)
                .get(OFRendererConstants.SWITCH_PORT_KEY);

		org.opendaylight.yang.gen.v1.urn
        .tbd.params.xml.ns.yang.network
        .topology.rev131021.NodeId sourceNodeId = extractTopologyNodeId(sourceNodeConnectorId);

		org.opendaylight.yang.gen.v1.urn
        .tbd.params.xml.ns.yang.network
        .topology.rev131021.NodeId destinationNodeId = extractTopologyNodeId(targetNodeConnectorId);

		List<Link> paths = graphService.getShortestPath(sourceNodeId, destinationNodeId);

		if (paths == null || paths.size() == 0)
			return "No data found.";

		return new Gson().toJson(paths);
	}

	@Override
	public String getDisjointPaths(String srcIP, String dstIP) {
		String sourceNodeConnectorId = intentMappingService.get(srcIP)
                .get(OFRendererConstants.SWITCH_PORT_KEY);
		String targetNodeConnectorId = intentMappingService.get(dstIP)
                .get(OFRendererConstants.SWITCH_PORT_KEY);

		org.opendaylight.yang.gen.v1.urn
        .tbd.params.xml.ns.yang.network
        .topology.rev131021.NodeId sourceNodeId = extractTopologyNodeId(sourceNodeConnectorId);

		org.opendaylight.yang.gen.v1.urn
        .tbd.params.xml.ns.yang.network
        .topology.rev131021.NodeId destinationNodeId = extractTopologyNodeId(targetNodeConnectorId);

		List<List<Link>> paths = graphService.getDisjointPaths(sourceNodeId, destinationNodeId);

		if (paths == null || paths.size() == 0)
			return "No data found.";

		return new Gson().toJson(paths);
	}
}
