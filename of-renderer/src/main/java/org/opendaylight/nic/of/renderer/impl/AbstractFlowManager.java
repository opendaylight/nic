/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.opendaylight.nic.of.renderer.api.FlowAction;
import org.opendaylight.nic.of.renderer.utils.FlowUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.of.renderer.utils.GenericTransactionUtils;
import org.opendaylight.openflowplugin.applications.pipeline_manager.PipelineManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.List;

public abstract class AbstractFlowManager {

    private final DataBroker dataBroker;
    protected static final Integer DEFAULT_IDLE_TIMEOUT = 0;
    protected static final Integer DEFAULT_HARD_TIMEOUT = 0;
    protected static final Integer DEFAULT_PRIORITY = 9000;
    private final PipelineManager pipelineManager;

    AbstractFlowManager(DataBroker dataBroker, PipelineManager pipelineManager) {
        this.dataBroker = dataBroker;
        this.pipelineManager = pipelineManager;
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

    protected Instructions createOutputInstructions(OutputPortValues... portValues) {
        List<Action> actionList = Lists.newArrayList();
        int order = 0;
        for (OutputPortValues outputPort: portValues) {
            if (outputPort.equals(OutputPortValues.NORMAL)) {
                actionList.add(FlowUtils.createOutputNormal(order));
                order++;
            }
            if (outputPort.equals(OutputPortValues.CONTROLLER)) {
                actionList.add(FlowUtils.createSendToControllerAction(order));
                order++;
            }
        }
        ApplyActions applyOutputActions = new ApplyActionsBuilder().setAction(actionList).build();
        Instruction outputInstruction = new InstructionBuilder().setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder().setApplyActions(applyOutputActions).build())
                .build();
        Instructions instructions = new InstructionsBuilder().setInstruction(
                ImmutableList.of(outputInstruction)).build();
        return instructions;
    }

    protected boolean writeDataTransaction(NodeId nodeId, FlowBuilder flowBuilder, FlowAction flowAction) {
        boolean result;

        pipelineManager.setTableId(nodeId, flowBuilder);

        InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(nodeId)).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flowBuilder.getTableId())).child(Flow.class, flowBuilder.getKey())
                .build();

        result = GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION,
                flowIID, flowBuilder.build(), flowAction);

        return result;
    }

}
