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
import org.opendaylight.nic.extensibility.actions.AuditActionType;
import org.opendaylight.nic.intent.AuxiliaryData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;

import com.google.common.collect.ImmutableList;

public class AuditTranslator extends BaseActionTranslator {

    @Override
    public Set<Instruction> translate(AuxiliaryData aux) {
        Set<org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Instruction> instructions = new HashSet<>();
        Action normal = new ActionBuilder().setAction(
                new OutputActionCaseBuilder().setOutputAction(
                        new OutputActionBuilder().setOutputNodeConnector(
                                new Uri(OutputPortValues.NORMAL.toString()))
                                .build()).build()).build();
        Action controller = new ActionBuilder().setAction(
                new OutputActionCaseBuilder().setOutputAction(
                        new OutputActionBuilder()
                                .setOutputNodeConnector(
                                        new Uri(OutputPortValues.CONTROLLER
                                                .toString())).build()).build())
                .build();
        ApplyActions apply = new ApplyActionsBuilder().setAction(
                ImmutableList.of(normal, controller)).build();

        // Wrap our Apply Action in an Instruction
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Instruction inst = new InstructionBuilder()
                .setOrder(0)
                .setInstruction(
                        new ApplyActionsCaseBuilder().setApplyActions(apply)
                                .build()).build();

        instructions.add(inst);
        return instructions;
    }

    @Override
    public ActionLabel actionLabel() {
        return AuditActionType.getInstance().label();
    }

}
