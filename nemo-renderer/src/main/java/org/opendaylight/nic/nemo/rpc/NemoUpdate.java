/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.nemo.rpc;

import static org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegment.ConditionParameterMatchPattern.LessThan;
import static org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegment.ConditionParameterMatchPattern.NotLessThan;
import static org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegment.PrecursorRelationOperator.And;
import static org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegment.PrecursorRelationOperator.None;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opendaylight.nic.nemo.renderer.NEMOIntentParser;
import org.opendaylight.nic.nemo.renderer.NEMOIntentParser.BandwidthOnDemandParameters;
import org.opendaylight.nic.nemo.renderer.NEMORenderer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ActionName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ConditionParameterName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ConditionSegmentId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ConnectionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ConnectionName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ConnectionType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.NodeName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.NodeType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.OperationId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.OperationName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.CommonRpcResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.NemoIntentService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.StructureStyleNemoUpdateInput;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.users.User;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.object.rev151010.connection.instance.EndNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.object.rev151010.connection.instance.EndNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.object.rev151010.node.instance.SubNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.action.instance.ParameterValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.action.instance.parameter.values.IntValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.action.instance.parameter.values.IntValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegment.ConditionParameterMatchPattern;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegment.PrecursorRelationOperator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegmentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.condition.segment.ConditionParameterTargetValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.operation.instance.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.operation.instance.ActionBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gwu
 *
 */
public class NemoUpdate implements NemoRpc {
    private static final ActionName ACTION_QOS_BANDWIDTH = new ActionName("qos-bandwidth");
    private static final ConnectionType P2P = new ConnectionType("p2p");
    private static final NodeType L2_GROUP = new NodeType("l2-group");
    private static final ConditionParameterName CONDITION_TIME = new ConditionParameterName("time");
    private static final DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm:ss");

    private static final Logger LOG = LoggerFactory.getLogger(NemoUpdate.class);
    private BandwidthOnDemandParameters params;

    public NemoUpdate(Intent intent) {
        try {
            params = NEMOIntentParser.parseBandwidthOnDemand(intent);
        } catch (Exception e) {
            LOG.info("Unable to parse BoD intent", e);
            params = null;
        }
    }

    public static StructureStyleNemoUpdateInput buildInput(final BandwidthOnDemandParameters params, User user) {

        List<Node> existingNodes = new ArrayList<>();
        if (user.getObjects() != null && user.getObjects().getNode() != null) {
            existingNodes.addAll(user.getObjects().getNode());
        }

        // convert parameters to StructureStyleNemoUpdateInput

        // All this code just to do the following:

        // CREATE Node <from> Type l2-group;
        // CREATE Node <to> Type l2-group;
        // CREATE Connection c1 Type p2p Endnodes <from>,<to>;
        // CREATE Operation o1 Priority 2 Target c1 Condition time>=9&&time<18 Action qos-bandwidth <bandwidth>

        Node nodeFrom = null;
        Node nodeTo = null;
        for (Node node : existingNodes) {
            if (node.getNodeName().getValue().equals(params.from)) {
                nodeFrom = node;
            }
            if (node.getNodeName().getValue().equals(params.to)) {
                nodeTo = node;
            }
        }
        List<Node> newNodes = new ArrayList<Node>();
        if (nodeFrom == null) {
            nodeFrom = node(params.from, L2_GROUP);
            newNodes.add(nodeFrom);
        }
        if (nodeTo == null) {
            nodeTo = node(params.to, L2_GROUP);
            newNodes.add(nodeTo);
        }

        Connection connection = connection(P2P, Arrays.asList(nodeFrom, nodeTo));
        List<Connection> connections = Arrays.asList(connection);

        Objects objects = new ObjectsBuilder().setNode(newNodes).setConnection(connections).build();

        LocalTime endTime = params.startTime.plus(params.duration);

        ConditionSegment cs1 = condition(1L, None, CONDITION_TIME, NotLessThan,
                params.startTime.toString(timeFormatter));
        ConditionSegment cs2 = condition(2L, And, CONDITION_TIME, LessThan, endTime.toString(timeFormatter));
        List<ConditionSegment> conditions = Arrays.asList(cs1, cs2);

        Action action = action(1L, ACTION_QOS_BANDWIDTH, params.bandwidth);
        List<Action> actions = Arrays.asList(action);

        Operation op = operation(2L, connection.getConnectionId(), conditions, actions);
        Operations operations = new OperationsBuilder().setOperation(Arrays.asList(op)).build();

        return new StructureStyleNemoUpdateInputBuilder().setObjects(objects).setOperations(operations)
                .setUserId(user.getUserId()).build();
    }

    private static Action action(long order, ActionName name, long value) {
        IntValue v = new IntValueBuilder().setOrder(1L).setValue(value).build();
        return new ActionBuilder().setActionName(name).setOrder(order)
                .setParameterValues(new ParameterValuesBuilder().setIntValue(Arrays.asList(v)).build()).build();
    }

    private static Node node(String name, NodeType type) {
        return new NodeBuilder().setNodeId(new NodeId(UUID.randomUUID().toString())).setNodeName(new NodeName(name))
                .setNodeType(type).setSubNode(new ArrayList<SubNode>()).build();
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

    private static Connection connection(ConnectionType type, List<Node> nodes) {

        final String connectionId = UUID.randomUUID().toString();

        List<EndNode> endNodes = endNodes(nodes);
        Connection connection = new ConnectionBuilder().setConnectionId(new ConnectionId(connectionId))
                .setConnectionName(new ConnectionName(NEMORenderer.NIC_PREFIX + connectionId)).setConnectionType(type)
                .setEndNode(endNodes).build();
        return connection;
    }

    private static ConditionSegment condition(long order, PrecursorRelationOperator relation,
            ConditionParameterName parameterName, ConditionParameterMatchPattern matchPattern, String targetValue) {
        return new ConditionSegmentBuilder()
                .setOrder(order)
                .setPrecursorRelationOperator(relation)
                .setConditionSegmentId(new ConditionSegmentId(UUID.randomUUID().toString()))
                .setConditionParameterName(parameterName)
                .setConditionParameterMatchPattern(matchPattern)
                .setConditionParameterTargetValue(
                        new ConditionParameterTargetValueBuilder().setStringValue(targetValue).build()).build();
    }

    private static Operation operation(long priority, ConnectionId target, List<ConditionSegment> conditions,
            List<Action> actions) {

        final String operationId = UUID.randomUUID().toString();

        return new OperationBuilder().setOperationId(new OperationId(operationId))
                .setOperationName(new OperationName(NEMORenderer.NIC_PREFIX + operationId)).setTargetObject(target)
                .setPriority(priority).setConditionSegment(conditions).setAction(actions).build();
    }

    @Override
    public RpcResult<? extends CommonRpcResult> apply(NemoIntentService nemoEngine, User user)
            throws InterruptedException, ExecutionException {
        if (params != null) {
            StructureStyleNemoUpdateInput updateInput = buildInput(params, user);
            return nemoEngine.structureStyleNemoUpdate(updateInput).get();
        } else {
            return null;
        }
    }

    @Override
    public boolean isInputValid() {
        return params != null;
    }
}
