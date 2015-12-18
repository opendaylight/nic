/*
 * Copyright (c) 2015 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.of.renderer.utils.MatchUtils;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MplsIntentFlowManager extends AbstractFlowManager {

    private List<String> endPointGroups = null;
    private Map<String, Map<String, String>> subjectsMapping = null;
    private Action action = null;
    private static final Logger LOG = LoggerFactory.getLogger(MplsIntentFlowManager.class);

    public MplsIntentFlowManager(DataBroker dataBroker, PipelineManager pipelineManager) {
        super(dataBroker, pipelineManager);
    }

    public void setEndPointGroups(List<String> endPointGroups) {
        this.endPointGroups = endPointGroups;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setSubjectsMapping(Map<String, Map<String, String>> subjectsMapping) {
        this.subjectsMapping = subjectsMapping;
    }

    @Override
    void pushFlow(NodeId nodeId, FlowAction flowAction) {
        // TODO Auto-generated method stub
    }

    /**
     * Create a IP Prefix Match and push MPLS label on switch
     * @param nodeId :NodeID of the switch on which the push MPLS label should be performed
     * @param flowAction :Add flow action
     * @param outputPort :Port to which packet should be sent to after pushing label
     */
    void pushMplsFlow(NodeId nodeId, FlowAction flowAction, String outputPort) {
        if (endPointGroups == null || action == null) {
            LOG.error("Endpoints and action cannot be null");
            return;
        }
        LOG.info("pushMPLSFlow on Node: {}", nodeId.getValue());

        MatchBuilder matchBuilder = new MatchBuilder();

        String endPointSrc = endPointGroups.get(OFRendererConstants.SRC_END_POINT_GROUP_INDEX);
        String endPointDst = endPointGroups.get(OFRendererConstants.DST_END_POINT_GROUP_INDEX);
        Ipv4Prefix srcPrefix = null;
        Ipv4Prefix dstPrefix = null;
        String label = null;
        try {
            srcPrefix = new Ipv4Prefix(subjectsMapping.get(endPointSrc).get(OFRendererConstants.IP_PREFIX_KEY));
            dstPrefix = new Ipv4Prefix(subjectsMapping.get(endPointDst).get(OFRendererConstants.IP_PREFIX_KEY));
            label = subjectsMapping.get(endPointDst).get(OFRendererConstants.MPLS_LABEL_KEY);
        } catch (Exception e) {
            LOG.error("Subject does not have mapping information for pushing MPLS label", e);
        }
        // Create IPv4 Prefix match
        MatchUtils.createIPv4PrefixMatch(srcPrefix, dstPrefix, matchBuilder);

        List<Long> labels = new ArrayList<>();
        labels.add(new Long(label));

        // Create Flow
        FlowBuilder flowBuilder = createFlowBuilder(matchBuilder);
        if (action instanceof Allow) {
            //bos field is set to 1 since we only use one MPLS label
            Short bos = 1;
            Instructions buildedInstructions = createMPLSIntentInstructions(labels, false, bos, outputPort, false);
            flowBuilder.setInstructions(buildedInstructions);
            LOG.info("Push MPLS label: {}", label, " to switch: {}", nodeId);
        } else if (action instanceof Block) {
            LOG.warn("For Block Action the Instructions are not set");
        } else {
            LOG.error("Invalid action: {}", action.getClass().getName());
            return;
        }
        writeDataTransaction(nodeId, flowBuilder, flowAction);
    }

    /**
     * Create a MPLS Label Match and pop MPLS label on switch
     * @param nodeId :NodeID of the switch on which the pop MPLS label should be performed
     * @param flowAction :Add flow action
     * @param outputPort :Port to which packet should be sent to after popping label
     */
    void popMplsFlow(NodeId nodeId, FlowAction flowAction, String outputPort) {
        if (endPointGroups == null || action == null) {
            LOG.error("Endpoints and action cannot be null");
            return;
        }
        LOG.info("popMplsFlow on Node {} and output on port {}", nodeId.getValue(), outputPort);

        String endPointDst = endPointGroups.get(OFRendererConstants.DST_END_POINT_GROUP_INDEX);
        String label = null;
        try {
            label = subjectsMapping.get(endPointDst).get(OFRendererConstants.MPLS_LABEL_KEY);
        } catch (Exception e) {
            LOG.error("Subject does not have mapping information for popping MPLS label", e);
        }

        List<Long> labels = new ArrayList<>();
        labels.add(new Long(label));
        MatchBuilder matchBuilder = MatchUtils.createMplsLabelBosMatch(new Long(label), true);
        // Create Flow
        FlowBuilder flowBuilder = createFlowBuilder(matchBuilder);
        if (action instanceof Allow) {
            //bos field is set to 1 since we only use one MPLS label
            Short bos = 1;
            Instructions builtInstructions = createMPLSIntentInstructions(labels, true, bos, outputPort, false);
            flowBuilder.setInstructions(builtInstructions);
            LOG.info("Pop MPLS label at switch: {}", nodeId.getValue());
            writeDataTransaction(nodeId, flowBuilder, flowAction);
        } else if (action instanceof Block) {
            LOG.warn("For Block Action the Instructions are not set");
        } else {
            LOG.error("Invalid action: {}", action.getClass().getName());
            return;
        }
    }

    /**
     * Create a MPLS label Match and forward the packet on switch
     * @param nodeId :NodeID of the switch on which the forward MPLS packet should be performed
     * @param flowAction :Add flow action
     * @param outputPort :Port to which packet should be forwarded
     */
    void forwardMplsFlow(NodeId nodeId, FlowAction flowAction, String outputPort) {
        if (endPointGroups == null || action == null) {
            LOG.error("Endpoints and action cannot be null");
            return;
        }
        LOG.info("forwardMplsFlow on Node: {}", nodeId.getValue());

        String endPointDst = endPointGroups.get(OFRendererConstants.DST_END_POINT_GROUP_INDEX);
        String label = null;
        try {
            label = subjectsMapping.get(endPointDst).get(OFRendererConstants.MPLS_LABEL_KEY);
        } catch (Exception e) {
            LOG.error("Subject does not have mapping information for forwarding MPLS packets", e);
        }
        // Create MPLS label match
        MatchBuilder matchBuilder = MatchUtils.createMplsLabelBosMatch(new Long(label), true);
        // Create Flow
        FlowBuilder flowBuilder = createFlowBuilder(matchBuilder);
        if (action instanceof Allow) {
            Instructions buildedInstructions = createMPLSIntentInstructions(null, false, null, outputPort, true);
            flowBuilder.setInstructions(buildedInstructions);
            LOG.info("Forward MPLS label at switch: {}", nodeId);
        } else if (action instanceof Block) {
            LOG.warn("For Block Action the Instructions are not set");
        } else {
            LOG.error("Invalid action: {}", action.getClass().getName());
            return;
        }
        writeDataTransaction(nodeId, flowBuilder, flowAction);
    }

    private FlowBuilder createFlowBuilder(MatchBuilder matchBuilder) {
        final Match match = matchBuilder.build();
        // Flow named for convenience and uniqueness
        String flowName = createFlowName();
        final FlowId flowId = new FlowId(flowName);
        final FlowKey key = new FlowKey(flowId);
        final FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setMatch(match);
        flowBuilder.setId(flowId);
        flowBuilder.setKey(key);
        flowBuilder.setBarrier(true);
        flowBuilder.setPriority(OFRendererConstants.DEFAULT_PRIORITY);
        flowBuilder.setFlowName(flowName);
        flowBuilder.setHardTimeout(OFRendererConstants.DEFAULT_HARD_TIMEOUT);
        flowBuilder.setIdleTimeout(OFRendererConstants.DEFAULT_IDLE_TIMEOUT);
        return flowBuilder;
    }

    @Override
    protected String createFlowName() {
        StringBuilder sb = new StringBuilder(OFRendererConstants.INTENT_MPLS_FLOW_NAME);
        sb.append(endPointGroups.get(OFRendererConstants.SRC_END_POINT_GROUP_INDEX));
        sb.append(endPointGroups.get(OFRendererConstants.DST_END_POINT_GROUP_INDEX));
        return sb.toString();
    }
}
