/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntentFlowManager extends AbstractFlowManager {

    private List<String> endPointGroups = null;
    private Map<String, Map<String, String>> subjectsMapping = null;
    private Action action = null;
    private static final Logger LOG = LoggerFactory.getLogger(IntentFlowManager.class);

    private static final Integer SRC_END_POINT_GROUP_INDEX = 0;
    private static final Integer DST_END_POINT_GROUP_INDEX = 1;
    private static final String ANY_MATCH = "any";
    private static final String INTENT_L2_FLOW_NAME = "L2_Rule_";
    private static final String MPLS_LABEL_KEY = "MPLS-label";
    private static final String NETWORK_PREFIX = "network-prefix";
    private static final String MPLS_PUSH_LABEL = "mpls-push-label";
    private static final String MPLS_POP_LABEL = "mpls-pop-label";

    public void setEndPointGroups(List<String> endPointGroups) {
        this.endPointGroups = endPointGroups;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setSubjectsMapping(Map<String, Map<String, String>> subjectsMapping) {
        this.subjectsMapping = subjectsMapping;
    }

    IntentFlowManager(DataBroker dataBroker, PipelineManager pipelineManager) {
        super(dataBroker, pipelineManager);
    }

    @Override
    void pushFlow(NodeId nodeId, FlowAction flowAction) {
        if (endPointGroups == null || action == null) {
            LOG.error("Endpoints and action cannot be null");
            return;
        }
        // Creating match object
        MatchBuilder matchBuilder = new MatchBuilder();
        // Creating Flow object
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create L2 match
        // TODO: Extend for L3 matches

        createEthMatch(endPointGroups, matchBuilder);

        // createMplsMatch(endPointGroups, subjectsMapping, matchBuilder);
        createIPv4PrefixMatch(matchBuilder, endPointGroups, subjectsMapping);

        // Create Flow
        flowBuilder = createFlowBuilder(matchBuilder);
        List<Long> labels = new ArrayList<>();
        labels.add(new Long("5567"));
        // TODO: Extend for other actions
        if (action instanceof Allow) {
            // List<Long> labels = getMPLSLabels(endPointGroups,
            // subjectsMapping);

            // Set allow action

            // Instructions buildedInstructions =
            // createOutputInstructions(OutputPortValues.NORMAL);
            Instructions buildedInstructions = createMPLSIntentInstructions(labels, false, new Short((short) 0), "2");

            // Instructions buildedInstructions =
            // createOutputInstructions(OutputPortValues.NORMAL);
            flowBuilder.setInstructions(buildedInstructions);

        } else if (action instanceof Block) {
            // For Block Action the Instructions are not set
            // If block added for readability
        } else {
            String actionClass = action.getClass().getName();
            LOG.error("Invalid action: {}", actionClass);
            return;
        }

        writeDataTransaction(nodeId, flowBuilder, flowAction);
    }

    private List<Long> getMPLSLabels(List<String> endPointGroups, Map<String, Map<String, String>> subjectsMapping) {
        List<Long> labels = new ArrayList<>();
        for (String endpointGroup : endPointGroups) {
            if (subjectsMapping.containsKey(endpointGroup)) {
                Map<String, String> values = subjectsMapping.get(endpointGroup);
                Long label = null;
                if (values.containsKey(MPLS_PUSH_LABEL))
                    label = Long.parseLong(values.get(MPLS_PUSH_LABEL));
                if (values.containsKey(MPLS_POP_LABEL))
                    label = Long.parseLong(values.get(MPLS_POP_LABEL));

                labels.add(label);
            }
        }
        return labels;
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
        flowBuilder.setPriority(DEFAULT_PRIORITY);
        flowBuilder.setFlowName(flowName);
        flowBuilder.setHardTimeout(DEFAULT_HARD_TIMEOUT);
        flowBuilder.setIdleTimeout(DEFAULT_IDLE_TIMEOUT);

        return flowBuilder;
    }

    private void createEthMatch(List<String> endPointGroups, MatchBuilder matchBuilder) {
        String endPointSrc = endPointGroups.get(SRC_END_POINT_GROUP_INDEX);
        String endPointDst = endPointGroups.get(DST_END_POINT_GROUP_INDEX);
        MacAddress srcMac = null;
        MacAddress dstMac = null;

        LOG.info("Creating block intent for endpoints: source{} destination {}", endPointSrc, endPointDst);
        try {
            if (!endPointSrc.equalsIgnoreCase(ANY_MATCH)) {
                srcMac = new MacAddress(endPointSrc);
            }
            if (!endPointDst.equalsIgnoreCase(ANY_MATCH)) {
                dstMac = new MacAddress(endPointDst);
            }
            MatchUtils.createEthMatch(matchBuilder, srcMac, dstMac);
        } catch (IllegalArgumentException e) {
            // LOG.error("Can only accept valid MAC addresses as subjects", e);
        }
    }

    // endPointGroups list received from Intent operation is a list of mac
    // addresses
    // For each mac address as key access the mapping information from
    // mapping-service
    // Get mpls label value from mapping information using MPLS-label as key
    // Finally add the label to the ProtocolMatchField of OF flow-rule
    private void createMplsMatch(List<String> endPointGroups, Map<String, Map<String, String>> subjectsMapping,
            MatchBuilder matchBuilder) {

        for (String value : endPointGroups) {
            if (subjectsMapping.containsKey(value) && subjectsMapping.get(value).containsKey((MPLS_LABEL_KEY))) {
                Long mplsLabel = new Long(subjectsMapping.get(value).get(MPLS_LABEL_KEY));
                // since we add only one MPLS label for now bos field is 1 or
                // true
                boolean bos = true;
                MatchUtils.createMplsLabelBosMatch(matchBuilder, mplsLabel, bos);
            }
        }
    }

    private void createIPv4PrefixMatch(MatchBuilder matchBuilder, List<String> endPointGroups,
            Map<String, Map<String, String>> subjectsMapping) {
        String endPointSrc = endPointGroups.get(SRC_END_POINT_GROUP_INDEX);
        String endPointDst = endPointGroups.get(DST_END_POINT_GROUP_INDEX);

        if (subjectsMapping.containsKey(endPointSrc) && subjectsMapping.get(endPointSrc).containsKey(NETWORK_PREFIX)) {
            String prefix = subjectsMapping.get(endPointSrc).get(NETWORK_PREFIX);
            Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
            Ipv4Prefix prefixx = new Ipv4Prefix(prefix);
            ipv4Match.setIpv4Source(prefixx);
            Ipv4Match i4m = ipv4Match.build();
            matchBuilder.setLayer3Match(i4m);
        }

        if (subjectsMapping.containsKey(endPointDst) && subjectsMapping.get(endPointDst).containsKey(NETWORK_PREFIX)) {
            String prefix = subjectsMapping.get(endPointDst).get(NETWORK_PREFIX);

            Ipv4MatchBuilder ipv4Match = new Ipv4MatchBuilder();
            Ipv4Prefix prefixx = new Ipv4Prefix(prefix);
            ipv4Match.setIpv4Destination(prefixx);
            Ipv4Match i4m = ipv4Match.build();
            matchBuilder.setLayer3Match(i4m);
        }
    }

    @Override
    protected String createFlowName() {
        StringBuilder sb = new StringBuilder();
        sb.append(INTENT_L2_FLOW_NAME);
        sb.append(endPointGroups.get(SRC_END_POINT_GROUP_INDEX));
        sb.append(endPointGroups.get(DST_END_POINT_GROUP_INDEX));
        return sb.toString();
    }
}
