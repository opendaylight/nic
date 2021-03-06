/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.of.renderer.listener.NetworkEventsService;
import org.opendaylight.nic.of.renderer.listener.TopologyListener;
import org.opendaylight.nic.of.renderer.utils.FlowUtils;
import org.opendaylight.nic.of.renderer.pipeline.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;

public class ArpFlowManager extends AbstractFlowManager implements TopologyListener {

    private final AtomicLong flowCookie = new AtomicLong();
    private final NetworkEventsService networkEventsService;

    public ArpFlowManager(final DataBroker dataBroker,
                          final PipelineManager pipelineManager,
                          final NetworkEventsService networkEventsService) {
        super(dataBroker, pipelineManager);
        this.networkEventsService = networkEventsService;
    }

    public void start() {
        networkEventsService.register(this);
    }

    @Override
    public void pushFlow(final NodeId nodeId, final FlowAction flowAction) {
        // Creating Flow object
        final FlowBuilder flowBuilder = createArpReplyToControllerFlow();
        // Write to MD-SAL
        super.writeDataTransaction(nodeId, flowBuilder, flowAction);
    }

    protected FlowBuilder createArpReplyToControllerFlow() {
        final FlowBuilder arpFlow = new FlowBuilder()
                .setPriority(OFRendererConstants.ARP_REPLY_TO_CONTROLLER_FLOW_PRIORITY)
                .setIdleTimeout(0)
                .setHardTimeout(0)
                .setCookie(new FlowCookie(BigInteger.valueOf(flowCookie.incrementAndGet())))
                .setFlags(new FlowModFlags(false, false, false, false, false));
        final EthernetMatch ethernetMatch = FlowUtils.createEthernetMatch();
        /** NOTE:
         * Setting layer 3 match seems to be messing with the flow ID
         * check for possible bug on openflow plugin side.
         * Use following code for specific ARP REQUEST or REPLY packet capture
         * ArpMatch arpMatch = FlowUtils.createArpMatch();
         */
        final Match match = new MatchBuilder().setEthernetMatch(ethernetMatch).build();//.setLayer3Match(arpMatch).build();
        arpFlow.setMatch(match);
        final Instructions instructions = createOutputInstructions(OutputPortValues.CONTROLLER, OutputPortValues.NORMAL);
        arpFlow.setInstructions(instructions);
        final String flowName = createFlowName();
        arpFlow.setFlowName(flowName);
        final FlowId flowId = new FlowId(flowName);
        arpFlow.setId(flowId);
        arpFlow.setKey(new FlowKey(flowId));
        return arpFlow;
    }

    @Override
    protected String createFlowName() {
        StringBuilder sb = new StringBuilder();
        sb.append(OFRendererConstants.ARP_REPLY_TO_CONTROLLER_FLOW_NAME);
        sb.append("_EthernetType_").append(flowCookie.get());
        return sb.toString();
    }

    @Override
    public void onSwitchAdd(NodeId switchAdded) {
        pushFlow(switchAdded, FlowAction.ADD_FLOW);
    }

    @Override
    public void onSwitchRemoved(NodeId switchRemoved) {
        pushFlow(switchRemoved, FlowAction.REMOVE_FLOW);
    }
}
