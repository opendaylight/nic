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
import org.opendaylight.nic.listeners.api.LinkDeleted;
import org.opendaylight.nic.listeners.api.LinkUp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
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
     * @param db                   - {@link DataBroker}
     */
    public NodeConnectorNotificationSupplierImpl(final DataBroker db) {
        super(db, FlowCapableNodeConnector.class);
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
//        final NodeConnectorUpdatedBuilder notifBuilder = new NodeConnectorUpdatedBuilder();
//        final FlowCapableNodeConnectorUpdatedBuilder connNotifBuilder =
//                new FlowCapableNodeConnectorUpdatedBuilder(object);
//        notifBuilder.setId(path.firstKeyOf(NodeConnector.class, NodeConnectorKey.class).getId());
//        notifBuilder.setNodeConnectorRef(new NodeConnectorRef(path));
//        notifBuilder.addAugmentation(FlowCapableNodeConnectorUpdated.class, connNotifBuilder.build());
//        return notifBuilder.build();
        //TODO: implement the builder for this notififcation
        return new LinkUp();
    }

    @Override
    public LinkDeleted deleteNotification(final InstanceIdentifier<FlowCapableNodeConnector> path) {
        Preconditions.checkArgument(path != null);
//        final NodeConnectorRemovedBuilder notifBuilder = new NodeConnectorRemovedBuilder();
//        notifBuilder.setNodeConnectorRef(new NodeConnectorRef(path));
//        return notifBuilder.build();
        //TODO: implement the builder for this notififcation
        return new LinkDeleted();
    }
}

