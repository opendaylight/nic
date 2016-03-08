/*
 * Copyright (c) 2015 - 2016 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.of.renderer.utils.FlowUtils;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ClearActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.clear.actions._case.ClearActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFlowManager {

    /**
     * Logger instance.
     */
    private static final Logger  LOG = LoggerFactory.getLogger(AbstractFlowManager.class);
    protected final DataBroker dataBroker;
    private final PipelineManager pipelineManager;
    private static final String L2_Rule_NAME = "L2_Rule_";

    AbstractFlowManager(DataBroker dataBroker, PipelineManager pipelineManager) {
        this.dataBroker = dataBroker;
        this.pipelineManager = pipelineManager;
    }

    protected abstract String createFlowName();

    /**
     * Programming a flow involves: 1. Creating a Flow object that has a match
     * and a list of instructions, 2. Adding Flow object as an augmentation to
     * the Node object in the inventory. 3. FlowProgrammer module of
     * OpenFlowPlugin picks up this data change and eventually program the
     * switch.
     * @param nodeId The OpenDaylight Inventory OpenFlow {@link NodeId}
     * @param flowAction The {@link FlowAction}
     */
    abstract void pushFlow(NodeId nodeId, FlowAction flowAction);

    /**
     * Creates a set of Instruction based on the port values
     * received.
     * @param portValues Represents ports (example LOCAL, CONTROLLER, etc) {@link OutputPortValues}}
     * @return OpenFlow Flow Instructions
     */
    protected Instructions createOutputInstructions(OutputPortValues... portValues) {
        List<Action> actionList = Lists.newArrayList();
        int order = 0;
        for (OutputPortValues outputPort : portValues) {
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
                .setInstruction(new ApplyActionsCaseBuilder().setApplyActions(applyOutputActions).build()).build();
        Instructions instructions = new InstructionsBuilder().setInstruction(ImmutableList.of(outputInstruction))
                .build();
        return instructions;
    }

    /**
     * Creates a set of redirect Instruction based on the port values
     * received.
     * @param outputPort OVS port to output the packet to
     * @return OpenFlow Flow Instructions
     */
    protected Instructions createRedirectIntentInstructions(String outputPort) {
        List<Action> actionList = Lists.newArrayList();
        int order = 0;
        actionList.add(FlowUtils.createOutputToPort(order++, outputPort));
        ApplyActions applyRedirectActions = new ApplyActionsBuilder().setAction(actionList).build();
        Instruction redirectInstruction = new InstructionBuilder().setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder().setApplyActions(applyRedirectActions).build()).build();
        Instructions instructions = new InstructionsBuilder().setInstruction(ImmutableList.of(redirectInstruction)).build();
        return instructions;
    }

    /**
     * To create MPLS VPN intents three actions are pushed for a match made
     * 1. push_mpls or pop_mpls action
     * 2. set_field to mpls label
     * 3. output to a switch port
     * @param labels List of MPLS labels
     * @param popLabel Boolean for pop action
     * @param bos Bottom of Stack value
     * @param outputPort OVS port to output the packet to
     * @param forward Boolean for forward MPLS packet action
     * @return A set of OpenFlow {@link Instructions} that have been construction
     */
    protected Instructions createMPLSIntentInstructions(List<Long> labels,
                                                        boolean popLabel,
                                                        Short bos,
                                                        String outputPort,
                                                        boolean forward) {
        return createMPLSIntentInstructions(labels, popLabel, bos, outputPort, forward, null);
    }

    /**
     * To create MPLS VPN intents three actions are pushed for a match made
     * 1. push_mpls or pop_mpls action
     * 2. set_field to mpls label
     * 3. output to a switch port
     * @param labels List of MPLS labels
     * @param popLabel Boolean for pop action
     * @param bos Bottom of Stack value
     * @param outputPort OVS port to output the packet to
     * @param forward Boolean for forward MPLS packet action
     * @param macAddress Server destination MAC Address
     * @return A set of OpenFlow {@link Instructions} that have been construction
     */
    protected Instructions createMPLSIntentInstructions(List<Long> labels,
                                                        boolean popLabel,
                                                        Short bos,
                                                        String outputPort,
                                                        boolean forward,
                                                        String macAddress) {
        int order = 0;
        List<Action> actionList = new ArrayList<>();
        if(!forward) {
            for (Long labelValue : labels) {
                actionList.add(FlowUtils.createMPLSAction(order++, popLabel));
                if (!popLabel) {
                    actionList.add(FlowUtils.createSetFieldMPLSLabelAction(order++, labelValue, bos));
                } else if (macAddress != null){
                    actionList.add(FlowUtils.createSetFieldDestinationMacAddress(order++, macAddress));
                }
            }
        }
        actionList.add(FlowUtils.createOutputToPort(order++, outputPort));
        ApplyActions applyMplsActions = new ApplyActionsBuilder().setAction(actionList).build();
        Instruction mplsInstruction = new InstructionBuilder().setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder().setApplyActions(applyMplsActions).build()).build();
        Instructions instructions = new InstructionsBuilder().setInstruction(ImmutableList.of(mplsInstruction)).build();
        return instructions;
    }

    /**
     * Creates a set of clear instructions to remove flows from switch
     * @return Instructions
     */
    protected Instructions createClearFlowsInstructions() {
        Instruction clearInstruction = new InstructionBuilder().setOrder(0)
                .setInstruction(new ClearActionsCaseBuilder().setClearActions(new ClearActionsBuilder().build()).build()).build();
        Instructions instructions = new InstructionsBuilder().setInstruction(ImmutableList.of(clearInstruction))
                .build();
        return instructions;
    }

    /**
     * Writes a Flow with a flow action on the Configuration
     * data store so that it can be applied to an OF switch.
     * @param nodeId The {@link NodeId} of the OF Switch
     * @param flowBuilder The {@link FlowBuilder} that is built and submitted
     * @param flowAction The {@link FlowAction} the flow action (ADD or REMOVE)
     * @return A boolean representing the transaction result
     */
    protected boolean writeDataTransaction(NodeId nodeId, FlowBuilder flowBuilder, FlowAction flowAction) {
        boolean result = false;
        MdsalUtils mdsal = new MdsalUtils(dataBroker);
        if (!pipelineManager.setTableId(nodeId, flowBuilder)) {
            flowBuilder.setTableId(OFRendererConstants.FALLBACK_TABLE_ID);
        }

        InstanceIdentifier<Flow> flowIID = InstanceIdentifier
                                               .builder(Nodes.class)
                                               .child(Node.class,
                                                      new NodeKey(nodeId)).augmentation(FlowCapableNode.class)
                                               .child(Table.class,
                                                      new TableKey(flowBuilder.getTableId()))
                                               .child(Flow.class, flowBuilder.getKey())
                                               .build();

        if (flowAction == FlowAction.ADD_FLOW) {
            result = mdsal.put(LogicalDatastoreType.CONFIGURATION, flowIID, flowBuilder.build());
        }
        else if(flowAction == FlowAction.REMOVE_FLOW) {
            result = mdsal.delete(LogicalDatastoreType.CONFIGURATION, flowIID);
        }

        return result;
    }

    /**
     * Creates a set of Instruction based on the port values and DSCP
     * received.
     * @param portValues Represents ports (example LOCAL, CONTROLLER, etc) {@link OutputPortValues}}
     * @param dscp Dscp Value.
     * @return OpenFlow Flow Instructions
     */
    protected Instructions createQoSInstructions(Dscp dscp, OutputPortValues... portValues) {
        List<Action> actionList = Lists.newArrayList();
        int order = 0;
        for (OutputPortValues outputPort : portValues) {
            if (outputPort.equals(OutputPortValues.NORMAL)) {
                actionList.add(FlowUtils.createOutputNormal(order));
                order++;
                actionList.add(FlowUtils.createQosNormal(order, dscp));
                order++;
            }
            if (outputPort.equals(OutputPortValues.CONTROLLER)) {
                actionList.add(FlowUtils.createSendToControllerAction(order));
                order++;
            }
        }
        ApplyActions applyOutputActions = new ApplyActionsBuilder().setAction(actionList).build();
        Instruction outputInstruction = new InstructionBuilder().setOrder(0)
                .setInstruction(new ApplyActionsCaseBuilder().setApplyActions(applyOutputActions).build()).build();
        Instructions instructions = new InstructionsBuilder().setInstruction(ImmutableList.of(outputInstruction))
                .build();
        return instructions;
    }

    /**
     * Read the flow id from the data store.
     * @param nodeId The {@link NodeId} of the OF Switch
     * @param flowBuilder The {@link FlowBuilder} that is built and submitted
     * @return flowId Flow Id.
     */
    protected String readDataTransaction(NodeId nodeId, FlowBuilder flowBuilder) {
        MdsalUtils mdsal = new MdsalUtils(dataBroker);
        InstanceIdentifier<Table> tableIID = InstanceIdentifier
                    .builder(Nodes.class)
                    .child(Node.class,
                           new NodeKey(nodeId)).augmentation(FlowCapableNode.class)
                    .child(Table.class,
                           new TableKey(OFRendererConstants.FALLBACK_TABLE_ID)).build();

        Table table = mdsal.read(LogicalDatastoreType.CONFIGURATION, tableIID);
        String destination = flowBuilder.getMatch().getEthernetMatch().getEthernetDestination().getAddress().getValue();
        String source = flowBuilder.getMatch().getEthernetMatch().getEthernetSource().getAddress().getValue();
        String logFlowId = L2_Rule_NAME + source + destination;
        if (table != null) {
            List<Flow> flows =  table.getFlow();
            for (Flow flow : flows) {
                String flowId = null;
                flowId = flow.getId().getValue().toString();
                if (flowId.contains(logFlowId)) {
                    return flowId;
                }
            }
        }
        LOG.info("Flow ID doesn't match with source{} and destination{}", source, destination);
        return null;
    }

}
