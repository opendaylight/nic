/*
 * Copyright (c) 2016 Serro LCC and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.opendaylight.nic.model.RendererCommonL3;
import org.opendaylight.nic.of.renderer.api.OFRendererQoSFlowService;
import org.opendaylight.nic.of.renderer.utils.FlowUtils;
import org.opendaylight.nic.of.renderer.utils.MatchUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by yrineu on 04/10/16.
 */
public class OFRendererQoSFlowServiceImpl implements OFRendererQoSFlowService<RendererCommonL3> {

    private final String FLOW_NAME = "QOS_FLOW_";
    private final short HIGHT_VIDEO_QUALITY_CONSTRAINT = 42;
    private final int INITIAL_ORDER = 0;

    @Override
    public void deployQoS(final RendererCommonL3 rendererCommon) {
        final MacAddress srcMacAddress = rendererCommon.getSrcMacAddress();
        final MacAddress dstMacAddress = rendererCommon.getDstMacAddress();

        final MatchBuilder matchBuilder = new MatchBuilder();
        MatchUtils.createEthMatch(matchBuilder, srcMacAddress, dstMacAddress);
        final Instructions qosInstructions = createQoSInstructions();

        final String flowName = createFlowName(rendererCommon.getId());
        final FlowBuilder flowBuilder = createFlowBuilder(flowName, matchBuilder, qosInstructions);
        //TODO: APPLY DATA TRANSACTION
    }

    @Override
    public void undeployQoS(Set<UUID> rendererCommonIds) {

    }

    private Instructions createQoSInstructions() {
        final List<Action> actions = Lists.newArrayList();
        actions.add(FlowUtils.createOutputNormal(INITIAL_ORDER));
        actions.add(FlowUtils.createQosNormal(1, new Dscp(HIGHT_VIDEO_QUALITY_CONSTRAINT)));

        final ApplyActionsBuilder applyOutputActions = new ApplyActionsBuilder();
        applyOutputActions.setAction(actions);

        final ApplyActionsCaseBuilder actionsCaseBuilder = new ApplyActionsCaseBuilder();
        actionsCaseBuilder.setApplyActions(applyOutputActions.build());

        final InstructionBuilder instructionBuilder = new InstructionBuilder();
        instructionBuilder.setOrder(INITIAL_ORDER);
        instructionBuilder.setInstruction(actionsCaseBuilder.build());

        final InstructionsBuilder instructionsBuilder = new InstructionsBuilder();
        instructionsBuilder.setInstruction(ImmutableList.of(instructionBuilder.build()));

        return instructionsBuilder.build();
    }

    private FlowBuilder createFlowBuilder(final String flowName,
                                          final MatchBuilder matchBuilder,
                                          final Instructions instructions) {
        final Match match = matchBuilder.build();
        final FlowId flowId = new FlowId(flowName);
        final FlowKey key = new FlowKey(flowId);
        final FlowBuilder flowBuilder = new FlowBuilder();

        flowBuilder.setMatch(match);
        flowBuilder.setId(flowId);
        flowBuilder.setKey(key);
        flowBuilder.setBarrier(true);
        flowBuilder.setPriority(OFRendererConstants.DEFAULT_PRIORITY);
        flowBuilder.setFlowName(FLOW_NAME);
        flowBuilder.setHardTimeout(OFRendererConstants.DEFAULT_HARD_TIMEOUT);
        flowBuilder.setIdleTimeout(OFRendererConstants.DEFAULT_IDLE_TIMEOUT);
        flowBuilder.setInstructions(instructions);

        return flowBuilder;
    }

    private String createFlowName(final UUID id) {
        return FLOW_NAME + id.toString();
    }
}
