/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.IEventService;
import org.opendaylight.nic.listeners.api.NodeDeleted;
import org.opendaylight.nic.listeners.api.NodeUp;
import org.opendaylight.nic.listeners.api.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdatedBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation define a contract between {@link FlowCapableNode} data object
 * and {@link NodeUp} and {@link NodeDeleted} notifications.
 */
public class NodeNotificationSupplierImpl extends
        AbstractNotificationSupplierItemRoot<FlowCapableNode, NodeUp, NodeDeleted, NodeUpdated> implements IEventService {

    private static final InstanceIdentifier<FlowCapableNode> FLOW_CAPABLE_NODE_IID =
            getNodeWildII().augmentation(FlowCapableNode.class);

    private static final Logger LOG = LoggerFactory.getLogger(NodeNotificationSupplierImpl.class);
    private static EventServiceRegistry serviceRegistry = EventServiceRegistry.getInstance();
    /**
     * Constructor register supplier as DataChangeLister and create wildCarded InstanceIdentifier.
     *
     * @param db                   - {@link DataBroker}
     */
    public NodeNotificationSupplierImpl(final DataBroker db) {
        super(db, FlowCapableNode.class, LogicalDatastoreType.OPERATIONAL);
        serviceRegistry.setEventTypeService(this, EventType.NODE_UPDATED, EventType.NODE_REMOVED);
    }

    @Override
    public InstanceIdentifier<FlowCapableNode> getWildCardPath() {
        return FLOW_CAPABLE_NODE_IID;
    }

    @Override
    public NodeUp createNotification(final FlowCapableNode object, final InstanceIdentifier<FlowCapableNode> ii) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(ii != null);
        final FlowCapableNodeUpdatedBuilder flowNodeNotifBuilder = new FlowCapableNodeUpdatedBuilder(object);
        FlowCapableNodeUpdated flowCapableNodeUpdated = flowNodeNotifBuilder.build();
        //TODO: Create a builder for NodeUp notification
        NodeUp nodeUpNotif = new NodeUpImpl(flowCapableNodeUpdated.getIpAddress(),
                getNodeId(ii));
        LOG.info("NicNotification created for Node up: IP", nodeUpNotif.getIp());
        return nodeUpNotif;
    }

    @Override
    public NodeDeleted deleteNotification(final FlowCapableNode flowCapableNode,
                                          final InstanceIdentifier<FlowCapableNode> path) {
        Preconditions.checkArgument(path != null);
        final NodeRemovedBuilder delNodeNotifBuilder = new NodeRemovedBuilder();
        delNodeNotifBuilder.setNodeRef(new NodeRef(path));
        NodeDeleted nodeDeleted = new NodeDeletedImpl(delNodeNotifBuilder.getNodeRef());
        LOG.info("NicNotification created for Node deleted");
        return nodeDeleted;
    }

    @Override
    public NodeUpdated updateNotification(final FlowCapableNode flowCapableNode,
                                          InstanceIdentifier<FlowCapableNode> path) {
        Preconditions.checkArgument(flowCapableNode != null);
        Preconditions.checkArgument(path != null);
        final NodeUpdatedBuilder nodeUpdatedBuilder = new NodeUpdatedBuilder();
        nodeUpdatedBuilder.setNodeRef(new NodeRef(path));
        return new NodeUpdatedImpl(nodeUpdatedBuilder.getNodeRef());
    }

    @Override
    public void addEventListener(IEventListener<?> listener) {
        serviceRegistry.registerEventListener(this, listener);
    }

    @Override
    public void removeEventListener(IEventListener<?> listener) {
        serviceRegistry.unregisterEventListener(this, listener);
    }

    @Override
    public EventType getCreateEventType() {
        return EventType.NODE_UPDATED;
    }

    @Override
    public EventType getDeleteEventType() {
        return EventType.NODE_REMOVED;
    }

    @Override
    public EventType getUpdateEventType() {
        return EventType.NODE_UPDATED;
    }

    @Override
    public Class<?> getCreateImplClass() {
        return NodeUpImpl.class;
    }

    @Override
    public Class<?> getDeleteImplClass() {
        return NodeDeletedImpl.class;
    }

    @Override
    public Class<?> getUpdateImplClass() {
        return NodeUpdatedImpl.class;
    }
}

