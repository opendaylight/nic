/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.nemo.renderer;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Conditions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.conditions.condition.Daily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.BandwidthConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ActionName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ConnectionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ConnectionName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ConnectionType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.NodeName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.NodeType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.OperationName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.PropertyName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.StructureStyleNemoUpdateInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.user.intent.Objects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.user.intent.ObjectsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.user.intent.Operations;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.user.intent.OperationsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.user.intent.objects.Connection;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.user.intent.objects.ConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.user.intent.objects.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.user.intent.objects.NodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.user.intent.operations.Operation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.user.intent.operations.OperationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.object.rev151010.connection.instance.EndNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.object.rev151010.connection.instance.EndNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.object.rev151010.connection.instance.Property;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.object.rev151010.connection.instance.PropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.object.rev151010.property.instance.PropertyValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.object.rev151010.property.instance.property.values.StringValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.operation.instance.ActionBuilder;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

/**
 *
 * @author gwu
 *
 */
public class NEMOIntentParser {

    private static final String OPERATION_O1 = "o1";
    private static final String ACTION_QOS_BANDWIDTH = "qos-bandwidth";
    private static final String CONNECTION_TYPE_P2P = "p2p";
    private static final String CONNECTION_C1 = "c1";
    private static final String PROPERTY_BANDWIDTH = "bandwidth";
    private static final String NODE_TYPE_L2_GROUP = "l2-group";

    private NEMOIntentParser() {
    }

    public static StructureStyleNemoUpdateInputBuilder parseBandwidthOnDemand(Intent intent) {

        List<Actions> actions = intent.getActions();
        Action action = !actions.isEmpty() ? actions.get(0).getAction() : null;

        // subjects in sorted order
        List<Subjects> subjects = Ordering.natural().onResultOf(new Function<Subjects, Short>() {
            @Override
            public Short apply(Subjects input) {
                return input.getOrder();
            }
        }).immutableSortedCopy(intent.getSubjects());

        BandwidthConstraint constraint = null;
        for (Constraints c : intent.getConstraints()) {
            if (c.getConstraints() instanceof BandwidthConstraint) {
                constraint = (BandwidthConstraint) c.getConstraints();
            }
        }

        List<Conditions> conditions = intent.getConditions();
        Daily condition = null;
        for (Conditions c : conditions) {
            if (c.getCondition() instanceof Daily) {
                condition = (Daily) c.getCondition();
            }
        }

        if (action instanceof Allow && constraint instanceof BandwidthConstraint && condition instanceof Daily
                && subjects.size() == 2) {

            if (subjects.get(0).getSubject() instanceof EndPointGroup
                    && subjects.get(1).getSubject() instanceof EndPointGroup) {

                String from = ((EndPointGroup) subjects.get(0).getSubject()).getEndPointGroup().getName();
                String to = ((EndPointGroup) subjects.get(1).getSubject()).getEndPointGroup().getName();
                String bandwidth = constraint.getBandwidthConstraint().getBandwidth();
                String startTime = condition.getDaily().getStartTime().getValue();
                String duration = condition.getDaily().getDuration().getValue();

                return asUpdateInputBuilder(from, to, bandwidth, startTime, duration);
            }

        }

        return null;

    }

    private static StructureStyleNemoUpdateInputBuilder asUpdateInputBuilder(String from, String to, String bandwidth,
            String startTime, String duration) {
        // convert parameters to StructureStyleNemoUpdateInput

        // All this code just to do the following:

        // CREATE Node <from> Type l2-group;
        // CREATE Node <to> Type l2-group;
        // CREATE Connection c1 Type p2p Endnodes <from>,<to> Property bandwidth:<bandwidth1>;
        // CREATE Operation o1 Target c1 Priority 0 Condition <condition> Action qos-bandwidth:<bandwidth2>;

        // TODO: get/set actual UUIDs
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        UUID c1Id = UUID.randomUUID();

        final NodeType nodeType = new NodeType(NODE_TYPE_L2_GROUP);
        Node nodeFrom = new NodeBuilder().setNodeId(new NodeId(fromId.toString())).setNodeName(new NodeName(from))
                .setNodeType(nodeType).build();
        Node nodeTo = new NodeBuilder().setNodeId(new NodeId(toId.toString())).setNodeName(new NodeName(to))
                .setNodeType(nodeType).build();
        List<Node> nodes = Arrays.asList(nodeFrom, nodeTo);

        EndNode endNodeFrom = new EndNodeBuilder().setNodeId(nodeFrom.getNodeId()).setOrder(1L).build();
        EndNode endNodeTo = new EndNodeBuilder().setNodeId(nodeTo.getNodeId()).setOrder(2L).build();
        List<EndNode> endNodes = Arrays.asList(endNodeFrom, endNodeTo);

        List<Property> properties = Arrays.asList(new PropertyBuilder()
                .setPropertyName(new PropertyName(PROPERTY_BANDWIDTH))
                .setPropertyValues(
                        new PropertyValuesBuilder().setStringValue(
                                Arrays.asList(new StringValueBuilder().setValue(bandwidth).setOrder(1L).build()))
                                .build()).build());

        Connection connection = new ConnectionBuilder().setConnectionId(new ConnectionId(c1Id.toString()))
                .setConnectionName(new ConnectionName(CONNECTION_C1))
                .setConnectionType(new ConnectionType(CONNECTION_TYPE_P2P)).setEndNode(endNodes)
                .setProperty(properties).build();
        List<Connection> connections = Arrays.asList(connection);

        Objects objects = new ObjectsBuilder().setNode(nodes).setConnection(connections).build();

        // TODO: specify conditions
        List<ConditionSegment> conditions = Arrays.asList();

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.operation.instance.Action> actions = Arrays
                .asList(new ActionBuilder().setActionName(new ActionName(ACTION_QOS_BANDWIDTH)).setOrder(1L).build());
        List<Operation> opsList = Arrays.asList(new OperationBuilder()
                .setOperationName(new OperationName(OPERATION_O1)).setTargetObject(connection.getConnectionId())
                .setPriority(0L).setConditionSegment(conditions).setAction(actions).build());
        Operations operations = new OperationsBuilder().setOperation(opsList).build();

        StructureStyleNemoUpdateInputBuilder builder = new StructureStyleNemoUpdateInputBuilder();
        builder.setObjects(objects);
        builder.setOperations(operations);
        return builder;

    }

}
