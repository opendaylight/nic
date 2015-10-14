/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.flow;

import com.google.common.collect.Lists;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.of.renderer.utils.GenericTransactionUtils;
import org.opendaylight.nic.of.renderer.utils.MatchUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OFRendererFlowManager implements OFRendererFlowService {

    private static Logger LOG = LoggerFactory.getLogger(OFRendererFlowManager.class);

    private static final Integer PRIORITY = 32768;
    private static final short DEFAULT_TABLE_ID = 0;
    private static final Integer DEFAULT_IDLE_TIMEOUT = 0;
    private static final Integer DEFAULT_HARD_TIMEOUT = 0;
    private static final Integer SRC_END_POINT_GROUP_INDEX = 0;
    private static final Integer DST_END_POINT_GROUP_INDEX = 1;
    private static final String ANY_MATCH = "any";

    private DataBroker dataBroker;

    public OFRendererFlowManager(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public void pushL2Flow(NodeId nodeId, List<String> endPointGroups,
                           org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action action,
                           FlowAction flowAction) {

        /*
         * Programming a flow involves: 1. Creating a Flow object that has a
         * match and a list of instructions, 2. Adding Flow object as an
         * augmentation to the Node object in the inventory. 3. FlowProgrammer
         * module of OpenFlowPlugin picks up this data change and eventually
         * program the switch.
         */

        // Creating match object
        MatchBuilder matchBuilder = new MatchBuilder();
        // Creating Flow object
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create L2 match
        // TODO: Extend for L3 matches
        createEthMatch(endPointGroups, matchBuilder);
        // Create Flow
        flowBuilder = createFlowBuilder(matchBuilder, endPointGroups);
        // TODO: Extend for other actions
        if (action instanceof Allow) {
            // Set allow action
            Instructions buildedInstructions = createInstructions(OutputPortValues.NORMAL);
            flowBuilder.setInstructions(buildedInstructions);

        } else if (action instanceof Block) {
            // For Block Action the Instructions are not set
            // If block added for readability
        } else {
            String actionClass = action.getClass().getName();
            LOG.error("Invalid action: {}", actionClass);
            return;
        }


        writeDataTransaction(nodeId, flowBuilder, flowAction);
    }

    private boolean writeDataTransaction(NodeId nodeId, FlowBuilder flowBuilder, FlowAction flowAction) {
        boolean result;

        InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(nodeId)).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flowBuilder.getTableId())).child(Flow.class, flowBuilder.getKey())
                .build();

        result = GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION,
                flowIID, flowBuilder.build(), flowAction);

        return result;
    }

    private FlowBuilder createFlowBuilder(MatchBuilder matchBuilder, List<String> endPointGroups) {
        final Match match = matchBuilder.build();
        //Flow named for convenience and uniqueness
        String flowIdStr = "L2_Rule_";
        flowIdStr += endPointGroups.get(SRC_END_POINT_GROUP_INDEX);
        flowIdStr += endPointGroups.get(DST_END_POINT_GROUP_INDEX);
        final FlowId flowId = new FlowId(flowIdStr);
        final FlowKey key = new FlowKey(flowId);
        final FlowBuilder flowBuilder = new FlowBuilder();

        flowBuilder.setMatch(match);
        flowBuilder.setId(flowId);
        flowBuilder.setKey(key);
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId(DEFAULT_TABLE_ID);
        flowBuilder.setPriority(PRIORITY);
        flowBuilder.setFlowName(flowIdStr);
        flowBuilder.setHardTimeout(DEFAULT_HARD_TIMEOUT);
        flowBuilder.setIdleTimeout(DEFAULT_IDLE_TIMEOUT);

        return flowBuilder;
    }

    private Instructions createInstructions(OutputPortValues portValues) {
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

    private void createEthMatch(List<String> endPointGroups, MatchBuilder matchBuilder) {
        String endPointSrc = endPointGroups.get(SRC_END_POINT_GROUP_INDEX);
        String endPointDst = endPointGroups.get(DST_END_POINT_GROUP_INDEX);
        MacAddress srcMac = null;
        MacAddress dstMac = null;

        LOG.info("Creating block intent for endpoints: source{} destination {}", endPointSrc, endPointDst);
        try {
            if (!endPointSrc.equalsIgnoreCase(ANY_MATCH)) {
                srcMac = new MacAddress(endPointSrc);
            }
            if (!endPointDst.equalsIgnoreCase(ANY_MATCH)) {
                dstMac = new MacAddress(endPointDst);
            }
            MatchUtils.createEthMatch(matchBuilder, srcMac, dstMac);
        } catch (IllegalArgumentException e) {
            LOG.error("Can only accept valid MAC addresses as subjects");
        }
    }
}