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
import org.opendaylight.nic.of.renderer.utils.TopologyUtils;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.IntentInvalidException;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.constraints.rev150122.FailoverType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.FailoverConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.ProtectionConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yrineu on 29/02/16.
 */
public class MPLSExecutor implements ActionStrategy {

    private interface ConstraintExecutor {
        void executeConstraint(Intent intent,
                               EndPointGroup srcEndPoint,
                               EndPointGroup dstEndPoint,
                               FlowAction flowAction,
                               Constraints constraints);
    }

    private Map<Class, ConstraintExecutor> constraintExecutorMap;

    private MplsIntentFlowManager mplsIntentFlowManager;
    private IntentMappingService intentMappingService;
    private OFRendererGraphService graphService;

    private static final Logger LOG = LoggerFactory.getLogger(MPLSExecutor.class);

    public MPLSExecutor(MplsIntentFlowManager mplsIntentFlowManager,
                        IntentMappingService intentMappingService,
                        OFRendererGraphService graphService) {
        this.mplsIntentFlowManager = mplsIntentFlowManager;
        this.intentMappingService = intentMappingService;
        this.graphService = graphService;
        this.constraintExecutorMap = new HashMap<>();
        populateConstraintExecutor();
    }

    private void populateConstraintExecutor() {
        constraintExecutorMap.put(ProtectionConstraint.class, (Intent intent,
                                                               EndPointGroup srcEndPoint,
                                                               EndPointGroup dstEndPoint,
                                                               FlowAction flowAction,
                                                               Constraints constraints) -> {

            ProtectionConstraint protectionConstraint = (ProtectionConstraint) constraints.getConstraints();
            boolean isProtected = protectionConstraint.getProtectionConstraint().isIsProtected();

            generateMplsFlows(intent, extractEndPointName(srcEndPoint),
                    extractEndPointName(dstEndPoint), null, flowAction, isProtected);
            LOG.trace("Protection is set to: {}", isProtected);
        });

        constraintExecutorMap.put(FailoverConstraint.class, (Intent intent,
                                                             EndPointGroup srcEndPoint,
                                                             EndPointGroup dstEndPoint,
                                                             FlowAction flowAction,
                                                             Constraints constraints) -> {
            FailoverConstraint failoverConstraint = (FailoverConstraint) constraints.getConstraints();
            FailoverType failoverType = failoverConstraint.getFailoverConstraint().getFailoverSelector();

            generateMplsFlows(intent, extractEndPointName(srcEndPoint),
                    extractEndPointName(dstEndPoint), failoverType, flowAction, false);
            LOG.trace("failoverType is set to: {}", failoverType);
        });
    }

    @Override
    public void execute(Intent intent, FlowAction flowAction) {
        try {
            Action actionContainer = IntentUtils.getAction(intent);
            List<String> endPointGroups = IntentUtils.extractEndPointGroup(intent);

            EndPointGroup source = IntentUtils.extractSrcEndPointGroup(intent);
            EndPointGroup target = IntentUtils.extractDstEndPointGroup(intent);

            mplsIntentFlowManager.setEndPointGroups(endPointGroups);
            mplsIntentFlowManager.setAction(actionContainer);
            mplsIntentFlowManager.setSubjectsMapping(extractSubjectDetails(endPointGroups));

            if (intent.getConstraints() != null) {
                for (Constraints constraints : intent.getConstraints()) {
                    ConstraintExecutor executor = constraintExecutorMap.get(constraints.getConstraints().getClass());
                    executor.executeConstraint(intent, source, target, flowAction, constraints);
                }
            }
        } catch (IntentInvalidException ie) {

        }
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
                .topology.rev131021.NodeId sourceNodeId = TopologyUtils.extractTopologyNodeId(sourceNodeConnectorId);
        org.opendaylight.yang.gen.v1.urn
                .tbd.params.xml.ns.yang.network
                .topology.rev131021.NodeId targetNodeId = TopologyUtils.extractTopologyNodeId(targetNodeConnectorId);
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

    private String extractEndPointName(EndPointGroup endPointGroup) {
        return endPointGroup.getEndPointGroup().getName();
    }
}
