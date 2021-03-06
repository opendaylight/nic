/**
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.pipeline;

import com.google.common.base.Optional;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.GoToTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.WriteActionsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.go.to.table._case.GoToTableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.set.field.match.SetFieldMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.NextTableMiss;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.TableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features.table.features.table.properties.TableFeatureProperties;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineManagerProviderImpl implements DataTreeChangeListener<Node>, PipelineManager {
    private static final Logger LOG = LoggerFactory.getLogger(PipelineManagerProviderImpl.class);
    private final DataBroker dataBroker;
    private ListenerRegistration<?> nodeListener;


    public PipelineManagerProviderImpl(final DataBroker dataBroker) {
        LOG.info("\nPipeline Manager service Initiated");
        this.dataBroker = dataBroker;
    }

    @Override
    public void start() {
        final InstanceIdentifier<Node> nodeIdentifier = InstanceIdentifier.create(Nodes.class).child(Node.class);
        nodeListener = dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<>(
                LogicalDatastoreType.OPERATIONAL, nodeIdentifier), this);
        LOG.info("new Pipeline Manager created: {}", this);
    }

    @Override
    public void onDataTreeChanged(Collection<DataTreeModification<Node>> changes) {
        for (DataTreeModification<Node> change: changes) {
            final DataObjectModification<Node> rootNode = change.getRootNode();
            switch (rootNode.getModificationType()) {
                case WRITE:
                    if (rootNode.getDataBefore() == null) {
                        createPipeline(rootNode.getDataAfter());
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void createPipeline(Node node) {
        List<Table> tableList = getTableList(node);
        for (Table table : tableList) {
            List<Short> nextIds = getNextTablesMiss(node.getId(), table.getId());
            if (nextIds.isEmpty()) {
                break;
            }
            Short nextId = Collections.min(nextIds);
            Short currentId = table.getId();
            addFlowGoto(node, currentId, nextId);
        }
    }

    private void addFlowGoto(Node node, Short currentId, Short nextId) {
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setTableId(currentId);
        flowBuilder.setHardTimeout(0);
        flowBuilder.setPriority(0);
        flowBuilder.setMatch(new MatchBuilder().build());
        flowBuilder.setInstructions(
                new InstructionsBuilder().setInstruction(Collections.singletonList(
                        new InstructionBuilder().setInstruction(
                                new GoToTableCaseBuilder().setGoToTable(
                                        new GoToTableBuilder().setTableId(nextId).build()
                                ).build()
                        ).setOrder(0).build()
                )).build());
        String flowIdStr = "PipelineManager";
        final FlowId flowId = new FlowId(flowIdStr);
        final FlowKey key = new FlowKey(flowId);
        flowBuilder.setKey(key);

        InstanceIdentifier<Flow> flowIID = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(node.getId())).augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flowBuilder.getTableId())).child(Flow.class, flowBuilder.getKey())
                .build();

        WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        transaction.put(LogicalDatastoreType.CONFIGURATION, flowIID, flowBuilder.build(), true);
        transaction.submit();
    }

    private List<TableFeatureProperties> getTableFeatureProperties(final NodeId nodeId, final Short tableId) {
        Node node = getDataObject(dataBroker.newReadOnlyTransaction(),
                InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId)));
        if (node == null) {
            return Collections.emptyList();
        }
        FlowCapableNode flowCapableNode = node.getAugmentation(FlowCapableNode.class);
        List<TableFeatures> features = flowCapableNode.getTableFeatures();
        if (features == null || features.isEmpty()) {
            return Collections.emptyList();
        }
        return features.get(tableId).getTableProperties().getTableFeatureProperties();
    }

    private List<Short> getNextTablesMiss(final NodeId nodeId, final Short tableId) {
        for (TableFeatureProperties tableFeatureProperties : getTableFeatureProperties(nodeId, tableId)) {
            if (tableFeatureProperties.getTableFeaturePropType() instanceof NextTableMiss) {
                NextTableMiss nextTableMiss = (NextTableMiss) tableFeatureProperties.getTableFeaturePropType();
                return nextTableMiss.getTablesMiss().getTableIds();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean setTableId(NodeId nodeId, FlowBuilder flowBuilder) {
        List<Table> tableList = getTableList(nodeId);
        for (Table table : tableList) {
            List<TableFeatureProperties> tableFeaturePropertiesList = getTableFeatureProperties(nodeId, table.getId());
            if (isFlowSupported(tableFeaturePropertiesList, flowBuilder)) {
                flowBuilder.setTableId(table.getId());
                return true;
            }
        }
        return false;
    }

    /* InventoryDataServiceUtil.getDataObject() */
    private static <T extends DataObject> T getDataObject(final ReadTransaction readOnlyTransaction, final InstanceIdentifier<T> identifier) {
        Optional<T> optionalData = null;
        try {
            optionalData = readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, identifier).get();
            if (optionalData.isPresent()) {
                return optionalData.get();
            }
        } catch (ExecutionException | InterruptedException e) {
            LOG.error("Read transaction for identifier {} failed.", identifier, e);
        }
        return null;
    }

    private List<Table> getTableList(NodeId nodeId) {
        Node node = PipelineManagerProviderImpl.getDataObject(dataBroker.newReadOnlyTransaction(),
                InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(nodeId)));
        return getTableList(node);
    }

    private List<Table> getTableList(Node node) {
        FlowCapableNode flowCapableNode = node.getAugmentation(FlowCapableNode.class);
        List<Table> tableList = flowCapableNode.getTable();
        Collections.sort(tableList, (o1, o2) -> o1.getId().compareTo(o2.getId()));
        return tableList;
    }

    private boolean isFlowSupported(List<TableFeatureProperties> tableFeaturePropertiesList, FlowBuilder flowBuilder) {
        Instructions flowBuilderInstructions = flowBuilder.getInstructions();
        if (flowBuilderInstructions == null) {
            return false;
        }
        List<SetFieldMatch> matchList = getMatchList(tableFeaturePropertiesList);
        return isMatchSupported(matchList, flowBuilder.getMatch())
                && isInstructionsSupported(tableFeaturePropertiesList, flowBuilderInstructions.getInstruction());
    }

    private boolean isInstructionsSupported(List<TableFeatureProperties> tableFeaturePropertiesList, List<Instruction> instructions) {
        for (Instruction instruction : instructions) {
            if (!isInstructionSupported(tableFeaturePropertiesList, instruction)) {
                return false;
            }
        }
        return true;
    }

    private boolean isInstructionSupported(List<TableFeatureProperties> tableFeaturePropertiesList, Instruction instruction) {
        List<Instruction> supportedInstructions = getInstructionList(tableFeaturePropertiesList);
        for (Instruction supportedInstructionProxy : supportedInstructions) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.Instruction supportedInstruction = supportedInstructionProxy.getInstruction();
            if (instruction.getInstruction().getImplementedInterface().equals(supportedInstruction.getImplementedInterface())) {
                if (instruction.getInstruction() instanceof ApplyActionsCase) {
                    ApplyActionsCase applyActionsCase = (ApplyActionsCase) instruction.getInstruction();
                    List<Action> supportedApplyActions = getApplyActionList(tableFeaturePropertiesList);
                    for (Action action : applyActionsCase.getApplyActions().getAction()) {
                        if (!isActionSupported(supportedApplyActions, action)) {
                            return false;
                        }
                    }
                    if (instruction.getInstruction() instanceof WriteActionsCase) {
                        WriteActionsCase writeActionsCase = (WriteActionsCase) instruction.getInstruction();
                        List<Action> supportedWriteActions = getWriteActionList(tableFeaturePropertiesList);
                        for (Action action : writeActionsCase.getWriteActions().getAction()) {
                            if (!isActionSupported(supportedWriteActions, action)) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean isActionSupported(List<Action> supportedApplyActions, Action action) {
        for (Action supportedApplyAction : supportedApplyActions) {
            if (supportedApplyAction.getAction().getImplementedInterface().equals(action.getAction().getImplementedInterface())) {
                return true;
            }
        }

        return false;
    }

    private boolean isFieldSupported(Class<? extends MatchField> field, List<SetFieldMatch> supportedFields) {
        for (SetFieldMatch supportedField : supportedFields) {
            if (isFieldMatch(field, supportedField.getMatchType())) {
                return true;
            }
        }

        return false;
    }

    private boolean isFieldMatch(Class<? extends MatchField> field, Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField> matchType) {
        return field.getSimpleName().equals(matchType.getSimpleName());
    }

    private boolean isMatchSupported(List<SetFieldMatch> supportedMatchList, Match match) {
        MatchConvertor matchConvertor = new MatchConvertor();
        List<MatchEntry> matchEntryList = matchConvertor.convert(match, null);
        for (MatchEntry matchEntry : matchEntryList) {
            if (!isFieldSupported(matchEntry.getOxmMatchField(), supportedMatchList)) {
                return false;
            }
        }
        return true;
    }

    private List<SetFieldMatch> getMatchList(List<TableFeatureProperties> tableFeaturePropertiesList) {
        for (TableFeatureProperties tableFeatureProperties : tableFeaturePropertiesList) {
            if (tableFeatureProperties.getTableFeaturePropType() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Match) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Match match = (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Match) tableFeatureProperties.getTableFeaturePropType();
                return match.getMatchSetfield().getSetFieldMatch();
            }
        }
        return Collections.emptyList();
    }

    private List<Instruction> getInstructionList(List<TableFeatureProperties> tableFeaturePropertiesList) {
        for (TableFeatureProperties tableFeatureProperties : tableFeaturePropertiesList) {
            if (tableFeatureProperties.getTableFeaturePropType() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Instructions) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Instructions instructions = (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.Instructions) tableFeatureProperties.getTableFeaturePropType();
                return instructions.getInstructions().getInstruction();
            }
        }
        return Collections.emptyList();
    }

    private List<Action> getApplyActionList(List<TableFeatureProperties> tableFeaturePropertiesList) {
        for (TableFeatureProperties tableFeatureProperties : tableFeaturePropertiesList) {
            if (tableFeatureProperties.getTableFeaturePropType() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActions) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActions actions = (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActions) tableFeatureProperties.getTableFeaturePropType();
                return actions.getApplyActions().getAction();
            }
        }
        return Collections.emptyList();
    }

    private List<Action> getWriteActionList(List<TableFeatureProperties> tableFeaturePropertiesList) {
        for (TableFeatureProperties tableFeatureProperties : tableFeaturePropertiesList) {
            if (tableFeatureProperties.getTableFeaturePropType() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActions) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActions actions = (org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.WriteActions) tableFeatureProperties.getTableFeaturePropType();
                return actions.getWriteActions().getAction();
            }
        }
        return Collections.emptyList();
    }


    @Override
    public void stop() {
        nodeListener.close();
        LOG.info("Pipeline Manager destroyed: {}", this);
    }
}
