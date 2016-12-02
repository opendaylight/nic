/*
 * Copyright (c) 2016 Open Networking Foundation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.impl;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;

import org.apache.commons.lang3.NotImplementedException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.common.model.FlowData;
import org.opendaylight.nic.of.renderer.utils.MatchUtils;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Created by icarocamelo on 2016-12-01.
 */
public class FlowManager extends AbstractFlowManager {
    private static final Logger LOG = LoggerFactory.getLogger(QosConstraintManager.class);
    FlowManager(DataBroker dataBroker, PipelineManager pipelineManager) {
        super(dataBroker, pipelineManager);
    }

    @Override
    protected String createFlowName() {
        return UUID.randomUUID().toString();
    }

    @Override
    void pushFlow(NodeId nodeId, FlowAction flowAction) {
        throw new NotImplementedException("deprecated");
    }

    @Override
    public void pushFlow(NodeId nodeId, FlowData renderer) {
        // Creating match object
        MatchBuilder matchBuilder = new MatchBuilder();
        // Creating Flow object

        Ipv4Prefix srcIpPrefix = renderer.getSrcIpPrefix().getIpv4Prefix();
        Ipv4Prefix dstIpPrefix = renderer.getDstIpPrefix().getIpv4Prefix();

        LOG.info("Creating intent for endpoints: source{} destination {}", srcIpPrefix, dstIpPrefix);

        MacAddress srcMac = renderer.getSrcMacAddress();
        MacAddress dstMac = renderer.getDstMacAddress();

        EthernetMatchBuilder ethernetMatch = new EthernetMatchBuilder();
        if (srcMac != null) {
            EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
            ethSourceBuilder.setAddress(srcMac);
            ethernetMatch.setEthernetSource(ethSourceBuilder.build());
        }
        if (dstMac != null) {
            EthernetDestinationBuilder ethDestinationBuilder = new EthernetDestinationBuilder();
            ethDestinationBuilder.setAddress(dstMac);
            ethernetMatch.setEthernetDestination(ethDestinationBuilder.build());
        }
        matchBuilder.setEthernetMatch(ethernetMatch.build());

        Ipv4MatchBuilder ipv4match = new Ipv4MatchBuilder();
        if (srcIpPrefix != null) {
            ipv4match.setIpv4Source(srcIpPrefix);
        }
        if (dstIpPrefix != null) {
            ipv4match.setIpv4Destination(dstIpPrefix);
        }
        matchBuilder.setLayer3Match(ipv4match.build());
        MatchUtils.createIPv4PrefixMatch(srcIpPrefix, dstIpPrefix, matchBuilder);

        Instructions buildedInstructions = createQoSInstructions(renderer.getDscp(), OutputPortValues.NORMAL);
        FlowBuilder flowBuilder = createFlowBuilder(matchBuilder);
        flowBuilder.setInstructions(buildedInstructions);

        writeDataTransaction(nodeId, flowBuilder, FlowAction.ADD_FLOW);
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
}
