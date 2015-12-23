/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.nemo.renderer;

import static org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegment.ConditionParameterMatchPattern.LessThan;
import static org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegment.ConditionParameterMatchPattern.NotLessThan;
import static org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegment.PrecursorRelationOperator.And;
import static org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegment.PrecursorRelationOperator.None;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opendaylight.nic.nemo.renderer.NEMOIntentParser.BandwidthOnDemandParameters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ActionName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ConditionParameterName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ConnectionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ConnectionName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ConnectionType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.NodeName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.NodeType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.OperationName;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.action.instance.ParameterValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.action.instance.parameter.values.StringValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.action.instance.parameter.values.StringValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegment.ConditionParameterMatchPattern;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegment.PrecursorRelationOperator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.condition.segment.ConditionParameterTargetValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.operation.instance.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.operation.instance.ActionBuilder;

/**
 * @author gwu
 *
 */
public class NemoInputBuilders {
    private static final OperationName OPERATION_O1 = new OperationName("o1");
    private static final ActionName ACTION_QOS_BANDWIDTH = new ActionName("qos-bandwidth");
    private static final ConnectionType P2P = new ConnectionType("p2p");
    private static final ConnectionName CONNECTION_C1 = new ConnectionName("c1");
    private static final NodeType L2_GROUP = new NodeType("l2-group");
    private static final ConditionParameterName CONDITION_TIME = new ConditionParameterName("time");
    private static final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm:ss");

    private NemoInputBuilders() {
    }

    public static StructureStyleNemoUpdateInputBuilder getUpdateBuilder(final BandwidthOnDemandParameters params) {
        // convert parameters to StructureStyleNemoUpdateInput

        // All this code just to do the following:

        // CREATE Node <from> Type l2-group;
        // CREATE Node <to> Type l2-group;
        // CREATE Connection c1 Type p2p Endnodes <from>,<to>;
        // CREATE Operation o1 Priority 2 Target c1 Condition time>=9&&time<18 Action qos-bandwidth <bandwidth>

        Node nodeFrom = node(params.from, L2_GROUP);
        Node nodeTo = node(params.to, L2_GROUP);
        List<Node> nodes = Arrays.asList(nodeFrom, nodeTo);

        Connection connection = connection(CONNECTION_C1, P2P, nodes);
        List<Connection> connections = Arrays.asList(connection);

        Objects objects = new ObjectsBuilder().setNode(nodes).setConnection(connections).build();

        LocalTime endTime = params.startTime.plus(params.duration);

        ConditionSegment cs1 = condition(1L, None, CONDITION_TIME, NotLessThan, params.startTime.toString(timeFormatter));
        ConditionSegment cs2 = condition(2L, And, CONDITION_TIME, LessThan, endTime.toString(timeFormatter));
        List<ConditionSegment> conditions = Arrays.asList(cs1, cs2);

        Action action = action(1L, ACTION_QOS_BANDWIDTH, params.bandwidth);
        List<Action> actions = Arrays.asList(action);

        Operation op = operation(OPERATION_O1, 2L, connection.getConnectionId(), conditions, actions);
        Operations operations = new OperationsBuilder().setOperation(Arrays.asList(op)).build();

        return new StructureStyleNemoUpdateInputBuilder().setObjects(objects).setOperations(operations);
    }

    private static Action action(long order, ActionName name, String value) {
        StringValue v = new StringValueBuilder().setOrder(1L).setValue(value).build();
        return new ActionBuilder().setActionName(name).setOrder(order)
                .setParameterValues(new ParameterValuesBuilder().setStringValue(Arrays.asList(v)).build()).build();
    }

    private static Node node(String name, NodeType type) {
        return new NodeBuilder().setNodeId(new NodeId(UUID.randomUUID().toString())).setNodeName(new NodeName(name))
                .setNodeType(type).build();
    }

    private static List<EndNode> endNodes(List<Node> nodes) {
        List<EndNode> endNodes = new ArrayList<>();
        long i = 1;
        for (Node node : nodes) {
            endNodes.add(new EndNodeBuilder().setOrder(i).setNodeId(node.getNodeId()).build());
            ++i;
        }
        return endNodes;
    }

    private static Connection connection(ConnectionName connectionC1, ConnectionType type, List<Node> nodes) {
        List<EndNode> endNodes = endNodes(nodes);
        Connection connection = new ConnectionBuilder().setConnectionId(new ConnectionId(UUID.randomUUID().toString()))
                .setConnectionName(connectionC1).setConnectionType(type).setEndNode(endNodes).build();
        return connection;
    }

    private static ConditionSegment condition(long order, PrecursorRelationOperator relation,
            ConditionParameterName parameterName, ConditionParameterMatchPattern matchPattern, String targetValue) {
        return new ConditionSegmentBuilder()
                .setOrder(order)
                .setPrecursorRelationOperator(relation)
                .setConditionParameterName(parameterName)
                .setConditionParameterMatchPattern(matchPattern)
                .setConditionParameterTargetValue(
                        new ConditionParameterTargetValueBuilder().setStringValue(targetValue).build()).build();
    }

    private static Operation operation(OperationName operationO1, long priority, ConnectionId target,
            List<ConditionSegment> conditions, List<Action> actions) {

        return new OperationBuilder().setOperationName(OPERATION_O1).setTargetObject(target).setPriority(priority)
                .setConditionSegment(conditions).setAction(actions).build();
    }

}
