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
import org.opendaylight.nic.listeners.api.LinkDeleted;
import org.opendaylight.nic.listeners.api.LinkUp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Implementation define a contract between {@link FlowCapableNodeConnector} data object
 * and {@link LinkUp} and {@link LinkDeleted} notifications.
 */
public class NodeConnectorNotificationSupplierImpl extends
        AbstractNotificationSupplierItemRoot<FlowCapableNodeConnector, LinkUp, LinkDeleted> {

    private static final InstanceIdentifier<FlowCapableNodeConnector> wildCardedInstanceIdent =
            getNodeWildII().child(NodeConnector.class).augmentation(FlowCapableNodeConnector.class);

    /**
     * Constructor register supplier as DataChangeLister and create wildCarded InstanceIdentifier.
     *
     * @param db - {@link DataBroker}
     */
    public NodeConnectorNotificationSupplierImpl(final DataBroker db) {
        super(db, FlowCapableNodeConnector.class, LogicalDatastoreType.OPERATIONAL);
    }

    @Override
    public InstanceIdentifier<FlowCapableNodeConnector> getWildCardPath() {
        return wildCardedInstanceIdent;
    }

    @Override
    public LinkUp createNotification(final FlowCapableNodeConnector object,
                                                   final InstanceIdentifier<FlowCapableNodeConnector> path) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(path != null);
        final NodeConnectorUpdatedBuilder notifBuilder = new NodeConnectorUpdatedBuilder();
        //TODO: implement the builder for this notififcation
        NodeConnectorId connectorId = path.firstKeyOf(NodeConnector.class).getId();
        final LinkUp notifLinkUp = new LinkUpImpl(object.getHardwareAddress(), object.getName(), connectorId);
        return notifLinkUp;
    }

    @Override
    public LinkDeleted deleteNotification(final FlowCapableNodeConnector nodeConnector,
                                          final InstanceIdentifier<FlowCapableNodeConnector> path) {
        Preconditions.checkArgument(path != null);
        //TODO: implement the builder for this notififcation
        return new LinkDeletedImpl();
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
    public Class getCreateImplClass() {
        return NodeUpImpl.class;
    }

    @Override
    public Class getDeleteImplClass() {
        return NodeDeletedImpl.class;
    }
}

