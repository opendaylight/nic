//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.translator.action;

import java.util.HashSet;
import java.util.Set;

import org.opendaylight.nic.extensibility.ActionLabel;
import org.opendaylight.nic.extensibility.actions.LatencyActionType;
import org.opendaylight.nic.intent.AuxiliaryData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetFieldCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatchBuilder;

import com.google.common.collect.ImmutableList;

public class LatencyTranslator extends BaseActionTranslator {

    public static final String LATENCY_DSCP_KEY = "latency_dscp";

    @Override
    public Set<Instruction> translate(AuxiliaryData aux) {
        Set<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Instruction> instructions = new HashSet<>();
        if (!aux.getData().containsKey(LATENCY_DSCP_KEY))
            throw new IllegalArgumentException("Translator requires '"
                    + LATENCY_DSCP_KEY + "' parameter");

        short dscpVal = Short.valueOf(aux.getData().get(LATENCY_DSCP_KEY));
        IpMatch dscpIpMatch = new IpMatchBuilder().setIpDscp(new Dscp(dscpVal))
                .build();
        Match dscpMatch = new MatchBuilder().setIpMatch(dscpIpMatch).build();
        Action latency = new ActionBuilder().setAction(
                new SetFieldCaseBuilder().setSetField(
                        new SetFieldBuilder(dscpMatch).build()).build())
                .build();
        ApplyActions applyLatency = new ApplyActionsBuilder().setAction(
                ImmutableList.of(latency)).build();
        new SetFieldMatchBuilder();

        // Wrap our Apply Action in an Instruction
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Instruction latencyInstruction = new InstructionBuilder()
                //
                .setOrder(0)
                .setInstruction(
                        new ApplyActionsCaseBuilder().setApplyActions(
                                applyLatency).build()).build();

        instructions.add(latencyInstruction);
        return instructions;
    }

    @Override
    public ActionLabel actionLabel() {
        return LatencyActionType.getInstance().label();
    }

}
