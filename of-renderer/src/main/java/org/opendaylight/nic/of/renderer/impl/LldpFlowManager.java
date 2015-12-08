/*
 * Copyright (c) 2015 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.impl;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;

public class LldpFlowManager extends AbstractFlowManager {

    private static final String LLDP_REPLY_TO_CONTROLLER_FLOW_NAME = "lldpReplyToController";
    private static final int LLDP_REPLY_TO_CONTROLLER_FLOW_PRIORITY = 9500;
    private final int LLDP_ETHER_TYPE = 35020;
    private final AtomicLong flowCookie = new AtomicLong();

    LldpFlowManager(DataBroker dataBroker, PipelineManager pipelineManager) {
        super(dataBroker, pipelineManager);
    }

    @Override
    protected String createFlowName() {
        StringBuilder sb = new StringBuilder();
        sb.append(LLDP_REPLY_TO_CONTROLLER_FLOW_NAME);
        sb.append("_EthernetType_").append(flowCookie.get());
        return sb.toString();
    }

    @Override
    void pushFlow(NodeId nodeId, FlowAction flowAction) {
        FlowBuilder flowBuilder = createLldpReplyToControllerFlow();
        writeDataTransaction(nodeId, flowBuilder, flowAction);
    }

    private FlowBuilder createLldpReplyToControllerFlow() {
        FlowBuilder lldpFlow = new FlowBuilder().setFlowName(createFlowName())
                .setIdleTimeout(0)
                .setHardTimeout(0)
                .setCookie(new FlowCookie(BigInteger.valueOf(flowCookie.incrementAndGet())))
                .setFlags(new FlowModFlags(false, false, false, false, false))
                .setPriority(LLDP_REPLY_TO_CONTROLLER_FLOW_PRIORITY);

        EthernetMatchBuilder ethernetMatchBuilder = new EthernetMatchBuilder()
                .setEthernetType(new EthernetTypeBuilder()
                .setType(new EtherType(Long.valueOf(LLDP_ETHER_TYPE))).build());
        Match match = new MatchBuilder().setEthernetMatch(ethernetMatchBuilder.build()).build();

        lldpFlow.setMatch(match);
        Instructions instructions = createOutputInstructions(OutputPortValues.CONTROLLER);
        lldpFlow.setInstructions(instructions);
        FlowId flowId = new FlowId(createFlowName());
        lldpFlow.setId(flowId);
        lldpFlow.setKey(new FlowKey(flowId));
        return lldpFlow;
    }
}
