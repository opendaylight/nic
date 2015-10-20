/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import org.opendaylight.nic.of.renderer.api.FlowAction;
import org.opendaylight.nic.of.renderer.utils.FlowUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.openflowplugin.applications.pipeline_manager.PipelineManager;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ArpFlowManager extends AbstractFlowManager {

    private static final Logger LOG = LoggerFactory.getLogger(ArpFlowManager.class);
    private static final String ARP_REPLY_TO_CONTROLLER_FLOW_NAME = "arpReplyToController";
    private static final int ARP_REPLY_TO_CONTROLLER_FLOW_PRIORITY = 10000;
    private final AtomicLong flowCookie = new AtomicLong();
    private final DataBroker dataBroker;

    public ArpFlowManager(DataBroker dataBroker, PipelineManager pipelineManager) {
        super(dataBroker, pipelineManager);
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
        FlowBuilder arpFlow = new FlowBuilder()
                .setPriority(ARP_REPLY_TO_CONTROLLER_FLOW_PRIORITY)
                .setIdleTimeout(0)
                .setHardTimeout(0)
                .setCookie(new FlowCookie(BigInteger.valueOf(flowCookie.incrementAndGet())))
                .setFlags(new FlowModFlags(false, false, false, false, false));
        EthernetMatch ethernetMatch = FlowUtils.createEthernetMatch();
        /** NOTE:
         * Setting layer 3 match seems to be messing with the flow ID
         * check for possible bug on openflow plugin side.
         * Use following code for specific ARP REQUEST or REPLY packet capture
         * ArpMatch arpMatch = FlowUtils.createArpMatch();
         */
//        ArpMatch arpMatch = FlowUtils.createArpMatch();
        Match match = new MatchBuilder().setEthernetMatch(ethernetMatch).build();//.setLayer3Match(arpMatch).build();
        arpFlow.setMatch(match);
        Instructions instructions = createOutputInstructions(OutputPortValues.CONTROLLER, OutputPortValues.NORMAL);
        arpFlow.setInstructions(instructions);
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
