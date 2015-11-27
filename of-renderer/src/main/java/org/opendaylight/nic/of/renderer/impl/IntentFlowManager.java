/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.of.renderer.utils.MatchUtils;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntentFlowManager extends AbstractFlowManager {

    private List<String> endPointGroups = null;
    private Action action = null;
    private static final Logger LOG = LoggerFactory.getLogger(IntentFlowManager.class);

    private static final Integer SRC_END_POINT_GROUP_INDEX = 0;
    private static final Integer DST_END_POINT_GROUP_INDEX = 1;
    private static final String ANY_MATCH = "any";
    private static final String INTENT_L2_FLOW_NAME = "L2_Rule_";

    public void setEndPointGroups(List<String> endPointGroups) {
        this.endPointGroups = endPointGroups;
    }

    public void setAction(Action action) {
        this.action = action;
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
        // Create Flow
        flowBuilder = createFlowBuilder(matchBuilder);
        // TODO: Extend for other actions
        if (action instanceof Allow) {
            // Set allow action
            Instructions buildedInstructions = createOutputInstructions(OutputPortValues.NORMAL);
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
            LOG.error("Can only accept valid MAC addresses as subjects", e);
        }
    }

    private void createIPv4PrefixMatch(Ipv4Prefix srcPrefix, Ipv4Prefix dstPrefix, MatchBuilder matchBuilder) {
        if (srcPrefix != null || dstPrefix != null) {
            long IPV4_LONG = 0x800;
            EthernetMatchBuilder eth = new EthernetMatchBuilder();
            EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
            ethTypeBuilder.setType(new EtherType(IPV4_LONG));
            eth.setEthernetType(ethTypeBuilder.build());
            matchBuilder.setEthernetMatch(eth.build());

            Ipv4MatchBuilder ipv4match = new Ipv4MatchBuilder();
            if (srcPrefix != null)
                ipv4match.setIpv4Source(srcPrefix);
            if (dstPrefix != null)
                ipv4match.setIpv4Destination(dstPrefix);

            matchBuilder.setLayer3Match(ipv4match.build());
        }
    }

    private void createIPv6PrefixMatch(Ipv6Prefix srcPrefix, Ipv6Prefix dstPrefix, MatchBuilder matchBuilder) {
        if (srcPrefix != null || dstPrefix != null) {
            long IPV6_LONG = 0x86DD;
            EthernetMatchBuilder eth = new EthernetMatchBuilder();
            EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
            ethTypeBuilder.setType(new EtherType(IPV6_LONG));
            eth.setEthernetType(ethTypeBuilder.build());
            matchBuilder.setEthernetMatch(eth.build());

            Ipv6MatchBuilder ipv4match = new Ipv6MatchBuilder();
            if (srcPrefix != null)
                ipv4match.setIpv6Source(srcPrefix);
            if (dstPrefix != null)
                ipv4match.setIpv6Destination(dstPrefix);

            matchBuilder.setLayer3Match(ipv4match.build());
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
