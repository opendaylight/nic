/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.listeners.api.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.Graph;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation define a contract between {@link Graph} data object
 * and {@link GraphEdgeAdded} notifications.
 */
public class GraphEdgeNotificationSupplierImpl extends
        AbstractNotificationSupplierItemRoot<Edges, GraphEdgeAdded, GraphEdgeDeleted, GraphEdgeUpdated> implements IEventService {

    private static final InstanceIdentifier<Edges> EDGE_IID =
            InstanceIdentifier.create(Graph.class).child(Edges.class);

    private static final Logger LOG = LoggerFactory.getLogger(GraphEdgeNotificationSupplierImpl.class);
    /**
     * Constructor register supplier as DataChangeLister and create wildCarded InstanceIdentifier.
     *
     * @param db                   - {@link DataBroker}
     */
    public GraphEdgeNotificationSupplierImpl(final DataBroker db) {
        super(db, Edges.class, LogicalDatastoreType.CONFIGURATION);
        serviceRegistry.setEventTypeService(this, EventType.GRAPH_EDGE_ADDED, EventType.GRAPH_EDGE_DELETED, EventType.GRAPH_EDGE_UPDATED);
    }

    @Override
    public InstanceIdentifier<Edges> getWildCardPath() {
        return EDGE_IID;
    }

    @Override
    public GraphEdgeAdded createNotification(final Edges object, final InstanceIdentifier<Edges> ii) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(ii != null);
        return new GraphEdgeAddedImpl(object);

    }

    @Override
    public GraphEdgeDeleted deleteNotification(final Edges object,
                                          final InstanceIdentifier<Edges> ii) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(ii != null);
        return new GraphEdgeDeletedImpl(object);
    }

    @Override
    public GraphEdgeUpdated updateNotification(final Edges object,
                                              InstanceIdentifier<Edges> ii) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(ii != null);
        return new GraphEdgeUpdatedImpl(object);
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
        return EventType.GRAPH_EDGE_ADDED;
    }

    @Override
    public EventType getDeleteEventType() {
        return EventType.GRAPH_EDGE_DELETED;
    }

    @Override
    public EventType getUpdateEventType() {
        return EventType.GRAPH_EDGE_UPDATED;
    }

    @Override
    public Class<?> getCreateImplClass() {
        return GraphEdgeAddedImpl.class;
    }

    @Override
    public Class<?> getDeleteImplClass() {
        return GraphEdgeDeletedImpl.class;
    }

    @Override
    public Class<?> getUpdateImplClass() {
        return GraphEdgeUpdated.class;
    }

}
