/*
 * Copyright (c) 2015 - 2016 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.strategy;

import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.opendaylight.nic.of.renderer.impl.MplsIntentFlowManager;
import org.opendaylight.nic.of.renderer.impl.NetworkGraphManager;
import org.opendaylight.nic.of.renderer.impl.OFRendererConstants;
import org.opendaylight.nic.of.renderer.utils.MappingServiceUtils;
import org.opendaylight.nic.of.renderer.utils.TopologyUtils;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.exceptions.IntentElementNotFoundException;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.constraints.rev150122.FailoverType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.FailoverConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.ProtectionConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yrineu on 29/02/16.
 */
public class MPLSExecutor implements ActionStrategy {

    private final MplsIntentFlowManager mplsIntentFlowManager;
    private final IntentMappingService intentMappingService;
    private final OFRendererGraphService graphService;

    private static final String NO_CONNECTOR_ID_FOUND_MSG = "Connector ID not found for EndPointGroup: ";
    private static final String NO_PATH_FOUND_MSG = "No Path found for intent with ID: ";

    private static final Logger LOG = LoggerFactory.getLogger(MPLSExecutor.class);

    public MPLSExecutor(final MplsIntentFlowManager mplsIntentFlowManager,
                        final IntentMappingService intentMappingService,
                        final OFRendererGraphService graphService) {
        this.mplsIntentFlowManager = mplsIntentFlowManager;
        this.intentMappingService = intentMappingService;
        this.graphService = graphService;
    }

    @Override
    public void execute(final Intent intent, final FlowAction flowAction) {
        try {
            final Action actionContainer = IntentUtils.getAction(intent);
            final List<String> endPointGroups = IntentUtils.extractEndPointGroup(intent);

            final Map<String, Map<String, String>> subjectDetails = MappingServiceUtils.extractSubjectDetails(intent,
                    intentMappingService);
            mplsIntentFlowManager.setEndPointGroups(endPointGroups);
            mplsIntentFlowManager.setAction(actionContainer);
            mplsIntentFlowManager.setSubjectsMapping(subjectDetails);

            if (intent.getConstraints() != null) {
                for (Constraints constraints : intent.getConstraints()) {
                    generateMplsFlows(intent, flowAction, constraints);
                }
            }
        } catch (IntentElementNotFoundException | IntentInvalidException ie) {
            //TODO: Perform fail over
            LOG.error(ie.getMessage());
        }
    }

    /**
     * Calls the appropriate functions to generate a shortest path
     * route between a source and a target.
     * @param intent
     * @param flowAction
     * @param constraints
     */
    private void generateMplsFlows(final Intent intent,
                                   final FlowAction flowAction,
                                   final Constraints constraints) {
        final List<Link> paths;
        try {
            paths = extractPathByFlowAction(intent, flowAction, constraints);
            LOG.info("Retrieved shortest path, there are {} hops.", paths.size());
            for (Link link : paths) {
                executeMplsIntentFlowManager(intent, link, flowAction);
            }
        } catch (IntentInvalidException ie) {
            //TODO: Perform fail over
        }
    }

    private String extractEndPointName(final EndPointGroup endPointGroup) {
        return endPointGroup.getEndPointGroup().getName();
    }

    private void executeMplsIntentFlowManager(final Intent intent, final Link link, final FlowAction flowAction)
            throws IntentInvalidException {

        final NodeId linkSourceNodeId = new NodeId(link.getSource().getSourceNode().getValue());
        final NodeId linkTargetNodeId = new NodeId(link.getDestination().getDestNode().getValue());
        final String linkSourceTp = link.getSource().getSourceTp().getValue();

        final EndPointGroup srcEndPointGroup = IntentUtils.extractSrcEndPointGroup(intent);
        final EndPointGroup dstEndPointGroup = IntentUtils.extractDstEndPointGroup(intent);

        if(containsLinkNodeId(srcEndPointGroup, linkSourceNodeId)) {
            mplsIntentFlowManager.pushMplsFlow(linkSourceNodeId, flowAction, linkSourceTp);
        } else if(containsLinkNodeId(dstEndPointGroup, linkTargetNodeId)) {
            mplsIntentFlowManager.forwardMplsFlow(linkSourceNodeId, flowAction, linkSourceTp);
            mplsIntentFlowManager.popMplsFlow(linkTargetNodeId, flowAction, extractConnectorId(dstEndPointGroup));
        } else {
            mplsIntentFlowManager.forwardMplsFlow(linkSourceNodeId, flowAction, linkSourceTp);
        }
    }

    private boolean containsLinkNodeId(final EndPointGroup endPointGroup, final NodeId linkNodeId) {
        final String nodeIdValue = extractNodeId(endPointGroup).getValue();
        return linkNodeId.getValue().equals(nodeIdValue);
    }

    private List<Link> extractPathByFlowAction(final Intent intent,
                                               final FlowAction flowAction,
                                               final Constraints constraints)
            throws IntentInvalidException {
        List<Link> result = new ArrayList<>();
        if (isProtectedOrSlowRoute(constraints)) {
            final EndPointGroup source = IntentUtils.extractSrcEndPointGroup(intent);
            final EndPointGroup target = IntentUtils.extractDstEndPointGroup(intent);
            switch (flowAction) {
                case ADD_FLOW:
                    NetworkGraphManager.ProtectedLinks.put(intent,
                            getDisjointPaths(source, target));
                    result = NetworkGraphManager.ProtectedLinks.get(intent).get(0);
                    break;
                case REMOVE_FLOW:
                    result = NetworkGraphManager.ProtectedLinks.get(intent).get(0);
                    NetworkGraphManager.ProtectedLinks.remove(intent);
                    break;
                default:
                    result = getDisjointPaths(source, target).get(0);
            }

        }
        if (result.isEmpty()) {
            throw new IntentInvalidException(NO_PATH_FOUND_MSG + intent.getId());
        }
        return result;
    }

    private List<List<Link>> getDisjointPaths(EndPointGroup srcEndPointGroup, EndPointGroup dstEndPointGroup) {
        return graphService.getDisjointPaths(extractNodeId(srcEndPointGroup), extractNodeId(dstEndPointGroup));
    }

    private org.opendaylight.yang.gen.v1.urn
            .tbd.params.xml.ns.yang.network
            .topology.rev131021.NodeId extractNodeId(final EndPointGroup endPointGroup)
            throws IntentElementNotFoundException {
        final String connectorId = intentMappingService.get(extractConnectorId(endPointGroup))
                .get(OFRendererConstants.SWITCH_PORT_KEY);
        return TopologyUtils.extractTopologyNodeId(connectorId);
    }

    private String extractConnectorId(final EndPointGroup endPointGroup) throws IntentElementNotFoundException {
        final String endPointName = extractEndPointName(endPointGroup);
        final String result = intentMappingService.get(endPointName)
                .get(OFRendererConstants.SWITCH_PORT_KEY);
        if(result == null || result.isEmpty()) {
            throw new IntentElementNotFoundException(NO_CONNECTOR_ID_FOUND_MSG + endPointName);
        }
        return result;
    }

    private boolean isProtectedOrSlowRoute(Constraints constraints) {
        boolean result = false;
        if (FailoverConstraint.class.isAssignableFrom(constraints.getConstraints().getImplementedInterface())) {
            final FailoverConstraint failoverConstraint = (FailoverConstraint) constraints.getConstraints();
            final FailoverType failoverType = failoverConstraint.getFailoverConstraint().getFailoverSelector();
            result = failoverType != null ? failoverType.equals(FailoverType.SlowReroute) : false;
        } else if (ProtectionConstraint.class.isAssignableFrom(constraints.getConstraints().getImplementedInterface())) {
            final ProtectionConstraint protectionConstraint = (ProtectionConstraint) constraints.getConstraints();
            result = protectionConstraint.getProtectionConstraint().isIsProtected();
        }
        return result;
    }
}