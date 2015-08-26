/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package nic.of.renderer;

import com.google.common.collect.Lists;
import nic.of.renderer.utils.GenericTransactionUtils;
import nic.of.renderer.utils.MatchUtils;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.DropActionCaseBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by saket on 8/19/15.
 */
public class OFRendererDataChangeListener implements DataChangeListener,AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(OFRendererDataChangeListener.class);

    private DataBroker dataBroker;
    private ListenerRegistration<DataChangeListener> ofRendererListener = null;
    private static final int  NUM_OF_SUPPORTED_EPG = 2;
    private static final int NUM_OF_SUPPORTED_ACTION = 1;

    public OFRendererDataChangeListener(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        // TODO: This should listen on something else
        ofRendererListener = dataBroker.registerDataChangeListener(
                LogicalDatastoreType.CONFIGURATION,
                InstanceIdentifier.builder(Intents.class)
                        .child(Intent.cla
                        .build(), this, AsyncDataBroker.DataChangeScope.SUBTREE);
    }

    // TODO: Move this to some common NIC utils
    private boolean verifyIntent(Intent intent) {
        if (intent.getId() == null) {
            LOG.warn("Intent ID is not specified {}", intent);
            return false;
        }
        if (intent.getActions() == null || intent.getActions().size() > NUM_OF_SUPPORTED_ACTION) {
            LOG.warn("Intent's action is either null or there is more than {} action {}"
                    , NUM_OF_SUPPORTED_ACTION, intent);
            return false;
        }
        if (intent.getSubjects() == null || intent.getSubjects().size() > NUM_OF_SUPPORTED_EPG) {
            LOG.warn("Intent's subjects is either null or there is more than {} subjects {}"
                    , NUM_OF_SUPPORTED_EPG, intent);
            return false;
        }
        return true;
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> asyncDataChangeEvent) {
        LOG.info("Intent tree changed");
        create(asyncDataChangeEvent.getCreatedData());
//        update(asyncDataChangeEvent.getUpdatedData());
//        delete(asyncDataChangeEvent);
    }

    private void create(Map<InstanceIdentifier<?>, DataObject> changes) {
        for (Map.Entry<InstanceIdentifier<?>, DataObject> created : changes.entrySet()) {
            if (created.getValue() != null && created.getValue() instanceof Intent) {
                Intent intent = (Intent) created.getValue();
                LOG.info("Creating intent with id {}.", intent);
                if (!verifyIntent(intent)) {
                    LOG.info("Intent verification failed");
                    return;
                }
                parseIntent(intent);
            }
        }
    }

    // TODO: Move this to some common NIC utils
    private void parseIntent(Intent intent) {
        // Retrieve the ID
        Uuid uuid = intent.getId();
        String intentID = uuid.getValue();

        // TODO: Use Mapping service to resolve the subjects
        // Retrieve the end points
        List<Subjects> listSubjects = intent.getSubjects();
        List<String> endPointGroups = new ArrayList<String>();
        for (Subjects subjects: listSubjects) {
            Subject subject = subjects.getSubject();
            if (!(subject instanceof EndPointGroup)) {
                LOG.info("Subject is not specified: {}", intentID);
                return;
            }
            EndPointGroup endPointGroup = (EndPointGroup) subject;

            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group
                    .EndPointGroup epg = endPointGroup.getEndPointGroup();
            if (epg == null) {
                LOG.info("End Point Group is not specified: {}", intentID);
                return;
            }
            endPointGroups.add(epg.getName());
        }
        String endPointSrc = endPointGroups.get(0);
        String endPointDst = endPointGroups.get(1);

        // TODO: Extend to support other actions
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action actionContainer =
                (org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action)
                        intent.getActions().get(0).getAction();
        // TODO: Push the flow
    }

    // TODO: Refactor this part
    private void pushL2Flow(NodeId nodeId, String dstMac,
                            NodeConnectorId ingressNodeConnectorId, NodeConnectorId egressNodeConnectorId,
                            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.
                                    intent.actions.Action action) {

        /* Programming a flow involves:
             * 1. Creating a Flow object that has a match and a list of instructions,
             * 2. Adding Flow object as an augmentation to the Node object in the inventory.
             * 3. FlowProgrammer module of OpenFlowPlugin picks up this data change and eventually program the switch.
        */

        //Creating match object
        MatchBuilder matchBuilder = new MatchBuilder();
        MatchUtils.createEthDstMatch(matchBuilder, new MacAddress(dstMac), null);
        MatchUtils.createInPortMatch(matchBuilder, ingressNodeConnectorId);

        // Instructions List Stores Individual Instructions
        InstructionsBuilder isb = new InstructionsBuilder();
        List<Instruction> instructions = Lists.newArrayList();
        InstructionBuilder ib = new InstructionBuilder();
        ApplyActionsBuilder aab = new ApplyActionsBuilder();
        ActionBuilder ab = new ActionBuilder();
        List<Action> actionList = Lists.newArrayList();

        if (action instanceof Allow) {
            // Set allow action
            OutputActionBuilder output = new OutputActionBuilder();
            output.setOutputNodeConnector(egressNodeConnectorId);
            output.setMaxLength(65535); //Send full packet and No buffer
            ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
            ab.setOrder(0);
            ab.setKey(new ActionKey(0));
            actionList.add(ab.build());
        } else if (action instanceof Block) {
            // Set block action
//            OutputActionBuilder output = new OutputActionBuilder()
//            output.setOutputNodeConnector(egressNodeConnectorId);
//            output.setMaxLength(65535); //Send full packet and No buffer
//            ab.setAction(new OutputActionCaseBuilder().setOutputAction(output.build()).build());
            // TODO : Resume here
            DropActionCase dropActionCase = new DropActionCaseBuilder().build();
            ab.setAction(dropActionCase);
            ab.setOrder(0);
            ab.setKey(new ActionKey(0));
            actionList.add(ab.build());
        } else {
            String actionClass = action.getClass().getName();
            LOG.error("Invalid action: {}", actionClass);
            return;
        }


        // Create Apply Actions Instruction
        aab.setAction(actionList);
        ib.setInstruction(new ApplyActionsCaseBuilder().setApplyActions(aab.build()).build());
        ib.setOrder(0);
        ib.setKey(new InstructionKey(0));
        instructions.add(ib.build());

        // Create Flow
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setMatch(matchBuilder.build());

        String flowId = "L2_Rule_" + dstMac;
        flowBuilder.setId(new FlowId(flowId));
        FlowKey key = new FlowKey(new FlowId(flowId));
        flowBuilder.setBarrier(true);
        flowBuilder.setTableId((short)0);
        flowBuilder.setKey(key);
        flowBuilder.setPriority(32768);
        flowBuilder.setFlowName(flowId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setIdleTimeout(0);
        flowBuilder.setInstructions(isb.setInstruction(instructions).build());

        InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(nodeId))
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flowBuilder.getTableId()))
                .child(Flow.class, flowBuilder.getKey())
                .build();
        GenericTransactionUtils.writeData(dataBroker, LogicalDatastoreType.CONFIGURATION,
                flowIID, flowBuilder.build(), true);
    }

    //    private void update(Map<InstanceIdentifier<?>, DataObject> changes) {
//
//    }
//
//    private void delete(Map<InstanceIdentifier<?>, DataObject> changes) {
//
//    }
    @Override
    public void close() throws Exception {

    }
}
