/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.nemo.renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.concurrent.Immutable;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ActionName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ConnectionName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ConnectionType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.NodeName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.NodeType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.ObjectId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.OperationName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.PropertyName;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.object.rev151010.connection.instance.EndNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.object.rev151010.connection.instance.EndNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.object.rev151010.connection.instance.Property;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.object.rev151010.connection.instance.PropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.object.rev151010.property.instance.PropertyValuesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.object.rev151010.property.instance.property.values.StringValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.condition.instance.ConditionSegment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.operation.instance.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.operation.rev151010.operation.instance.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.user.rev151010.UserInstance;

/**
 * This class is immutable so all fields are public, final, and immutable.
 *
 * @author gwu
 *
 */
@Immutable
public class NEMOData {

    public final String from;
    public final String to;
    public final String bandwidth;
    public final String startTime;
    public final String duration;

    public NEMOData(String from, String to, String bandwidth, String startTime, String duration) {
        this.from = from;
        this.to = to;
        this.bandwidth = bandwidth;
        this.startTime = startTime;
        this.duration = duration;
    }

    public StructureStyleNemoUpdateInput asStructureStyleNemoUpdateInput(UserInstance userInstance) {
        // convert parameters to StructureStyleNemoUpdateInput

        // All this code just to do the following:

        // CREATE Node <from> Type Type l2-group;
        // CREATE Node <to> Type Type l2-group;
        // CREATE Connection c1 Type p2p Endnodes <from>,<to> Property bandwidth:<bandwidth1>;
        // CREATE Operation o1 Target c1 Priority 0 Condition <condition> Action qos-bandwidth:<bandwidth2>;

        List<Node> nodes = new ArrayList<>();
        final NodeType nodeType = new NodeType("l2-group");
        nodes.add(new NodeBuilder().setNodeId(new NodeId(from)).setNodeName(new NodeName(from)).setNodeType(nodeType)
                .build());
        nodes.add(new NodeBuilder().setNodeId(new NodeId(to)).setNodeName(new NodeName(to)).setNodeType(nodeType)
                .build());

        List<EndNode> endNodes = new ArrayList<>();
        endNodes.add(new EndNodeBuilder().setNodeId(new NodeId(from)).setOrder(1L).build());
        endNodes.add(new EndNodeBuilder().setNodeId(new NodeId(to)).setOrder(2L).build());

        List<Property> properties = Arrays.asList(new PropertyBuilder()
                .setPropertyName(new PropertyName("bandwidth"))
                .setPropertyValues(
                        new PropertyValuesBuilder().setStringValue(
                                Arrays.asList(new StringValueBuilder().setValue(bandwidth).setOrder(1L).build()))
                                .build()).build());

        List<Connection> connections = Arrays.asList(new ConnectionBuilder()
                .setConnectionName(new ConnectionName("c1")).setConnectionType(new ConnectionType("p2p"))
                .setEndNode(endNodes).setProperty(properties).build());

        Objects objects = new ObjectsBuilder().setNode(nodes).setConnection(connections).build();

        // TODO: specify conditions
        List<ConditionSegment> conditions = Arrays.asList();

        List<Action> actions = Arrays.asList(new ActionBuilder().setActionName(new ActionName("qos-bandwidth"))
                .setOrder(1L).build());
        List<Operation> opsList = Arrays.asList(new OperationBuilder().setOperationName(new OperationName("o1"))
                .setTargetObject(new ObjectId("c1")).setPriority(0L).setConditionSegment(conditions).setAction(actions)
                .build());
        Operations operations = new OperationsBuilder().setOperation(opsList).build();

        StructureStyleNemoUpdateInputBuilder builder = new StructureStyleNemoUpdateInputBuilder();
        builder.setObjects(objects).setOperations(operations);
        NEMOInputHelper.setUserInstance(builder, userInstance);

        return builder.build();
    }
}
