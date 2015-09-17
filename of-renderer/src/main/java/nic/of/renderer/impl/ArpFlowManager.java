/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package nic.of.renderer.impl;

import com.google.common.collect.ImmutableList;
import nic.of.renderer.api.FlowAction;
import nic.of.renderer.utils.ArpFlowUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ArpFlowManager extends AbstractFlowManager {

    private static final Logger LOG = LoggerFactory.getLogger(ArpFlowManager.class);
    private static final String ARP_REPLY_TO_CONTROLLER_FLOW_NAME = "arpReplyToController";
    private static final int ARP_REPLY_TO_CONTROLLER_FLOW_PRIORITY = 10000;
    private static final Instruction SEND_TO_CONTROLLER_INSTRUCTION;
    private final AtomicLong flowCookie = new AtomicLong();
    private final DataBroker dataBroker;

    static {
        ApplyActions applyActions = new ApplyActionsBuilder().setAction(
                ImmutableList.of(ArpFlowUtils.createSendToControllerAction(0))).build();
        SEND_TO_CONTROLLER_INSTRUCTION = new InstructionBuilder().setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder().setApplyActions(applyActions).build())
                .build();
    }

    public ArpFlowManager(DataBroker dataBroker) {
        super(dataBroker);
        this.dataBroker = dataBroker;
    }

    @Override
    public void pushFlow(NodeId nodeId, FlowAction flowAction) {
        // Creating Flow object
        FlowBuilder flowBuilder = createArpReplyToControllerFlow();
        // Write to MD-SAL
        writeDataTransaction(nodeId, flowBuilder, flowAction);
    }

    private FlowBuilder createArpReplyToControllerFlow() {
        FlowBuilder arpFlow = new FlowBuilder().setTableId(DEFAULT_TABLE_ID)
                .setPriority(ARP_REPLY_TO_CONTROLLER_FLOW_PRIORITY)
//                .setBufferId(OFConstants.OFP_NO_BUFFER)
                .setIdleTimeout(0)
                .setHardTimeout(0)
                .setCookie(new FlowCookie(BigInteger.valueOf(flowCookie.incrementAndGet())))
                .setFlags(new FlowModFlags(false, false, false, false, false));
        EthernetMatch ethernetMatch = ArpFlowUtils.createEthernetMatch();
        // TODO: Setting layer 3 match seems to be messing with the flow ID
        // TODO: check for possible bug on openflow plugin side
//        ArpMatch arpMatch = ArpFlowUtils.createArpMatch();
        Match match = new MatchBuilder().setEthernetMatch(ethernetMatch).build();//.setLayer3Match(arpMatch).build();
        arpFlow.setMatch(match);
        arpFlow.setInstructions(new InstructionsBuilder().setInstruction(
                ImmutableList.of(SEND_TO_CONTROLLER_INSTRUCTION)).build());
        String flowName = createFlowName();
        arpFlow.setFlowName(flowName);
        FlowId flowId = new FlowId(flowName);
        arpFlow.setId(flowId);
        arpFlow.setKey(new FlowKey(flowId));
        return arpFlow;
    }

    @Override
    protected String createFlowName() {
        StringBuilder sb = new StringBuilder();
        sb.append(ARP_REPLY_TO_CONTROLLER_FLOW_NAME);
        sb.append("_EthernetType_").append(flowCookie.get());
        return sb.toString();
    }
}
