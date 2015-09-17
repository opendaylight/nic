/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package nic.of.renderer.impl;

import com.google.common.collect.Lists;
import nic.of.renderer.api.FlowAction;
import nic.of.renderer.utils.GenericTransactionUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.List;

public abstract class AbstractFlowManager {

    private final DataBroker dataBroker;
    protected static final short DEFAULT_TABLE_ID = 0;
    protected static final Integer DEFAULT_IDLE_TIMEOUT = 0;
    protected static final Integer DEFAULT_HARD_TIMEOUT = 0;
    protected static final Integer DEFAULT_PRIORITY = 32768;

    AbstractFlowManager(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    protected abstract String createFlowName();

    /**
     * Programming a flow involves: 1. Creating a Flow object that has a
     * match and a list of instructions, 2. Adding Flow object as an
     * augmentation to the Node object in the inventory. 3. FlowProgrammer
     * module of OpenFlowPlugin picks up this data change and eventually
     * program the switch.
     */
    abstract void pushFlow(NodeId nodeId, FlowAction flowAction);

    protected Instructions createInstructions(OutputPortValues portValues) {
        // Instructions List Stores Individual Instructions
        List<Instruction> instructions = Lists.newArrayList();
        InstructionsBuilder instructionsBuilder = new InstructionsBuilder();
        InstructionBuilder instructionBuilder = new InstructionBuilder();
        ApplyActionsBuilder applyActionBuilder = new ApplyActionsBuilder();
        ActionBuilder actionBuilder = new ActionBuilder();
        List<Action> actionList = Lists.newArrayList();
        OutputActionBuilder output = new OutputActionBuilder();
        OutputActionCaseBuilder actionCaseBuilder;
        ApplyActionsCaseBuilder applyActionsCaseBuilder;

        output.setOutputNodeConnector(new Uri(portValues.toString()));

        actionCaseBuilder = new OutputActionCaseBuilder();
        actionCaseBuilder.setOutputAction(output.build());

        actionBuilder.setAction(actionCaseBuilder.build());
        actionBuilder.setOrder(0);
        actionBuilder.setKey(new ActionKey(0));
        actionList.add(actionBuilder.build());

        // Create Apply Actions Instruction
        applyActionBuilder.setAction(actionList);
        applyActionsCaseBuilder = new ApplyActionsCaseBuilder();
        applyActionsCaseBuilder.setApplyActions(applyActionBuilder.build());

        instructionBuilder.setInstruction(applyActionsCaseBuilder.build());
        instructionBuilder.setOrder(0);
        instructionBuilder.setKey(new InstructionKey(0));
        instructions.add(instructionBuilder.build());

        instructionsBuilder.setInstruction(instructions);

        return instructionsBuilder.build();
    }

    protected boolean writeDataTransaction(NodeId nodeId, FlowBuilder flowBuilder, FlowAction flowAction) {
        boolean result;

        InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(nodeId)).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flowBuilder.getTableId())).child(Flow.class, flowBuilder.getKey())
                .build();

        result = GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION,
                flowIID, flowBuilder.build(), flowAction);

        return result;
    }

}
