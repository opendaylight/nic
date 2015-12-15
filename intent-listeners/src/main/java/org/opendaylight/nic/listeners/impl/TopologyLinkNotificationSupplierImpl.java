/*
 * Copyright (c) 2015 Inocybe inc. and others.  All rights reserved.
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
import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.nic.listeners.api.TopologyLinkDeleted;
import org.opendaylight.nic.listeners.api.TopologyLinkUp;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class TopologyLinkNotificationSupplierImpl extends
        AbstractNotificationSupplierItemRoot<Link,
                                             TopologyLinkUp,
                                             TopologyLinkDeleted,
                                             NicNotification>
                                                  implements IEventService {

    public TopologyLinkNotificationSupplierImpl(final DataBroker db) {
        super(db, Link.class, LogicalDatastoreType.OPERATIONAL);
        serviceRegistry.setEventTypeService(this,
                                            EventType.TOPOLOGY_LINK_DISCOVERED,
                                            EventType.TOPOLOGY_LINK_REMOVED,
                                            EventType.TOPOLOGY_LINK_UPDATED);
    }

    @Override
    public InstanceIdentifier<Link> getWildCardPath() {
        return mdsalMapper.getLinkWildII();
    }

    @Override
    public TopologyLinkUp createNotification(final Link topoLink,
                                             final InstanceIdentifier<Link> path) {
        Preconditions.checkNotNull(topoLink);
        Preconditions.checkNotNull(path);
        final LinkBuilder linkBuilder = new LinkBuilder(topoLink);
        return new TopologyLinkUpImpl(linkBuilder.build(), linkBuilder.getLinkId());
    }

    @Override
    public TopologyLinkDeleted deleteNotification(final Link topoLink,
                                                  final InstanceIdentifier<Link> path) {
        Preconditions.checkNotNull(topoLink);
        Preconditions.checkNotNull(path);
        final LinkBuilder linkBuilder = new LinkBuilder(topoLink);
        return new TopologyLinkDeletedImpl(linkBuilder.build());
    }

    @Override
    public NicNotification updateNotification(final Link topoLink,
                                              final InstanceIdentifier<Link> path) {
        Preconditions.checkNotNull(topoLink);
        Preconditions.checkNotNull(path);
        final LinkBuilder linkBuilder = new LinkBuilder(topoLink);
        return new TopologyLinkUpdatedImpl(linkBuilder.build(), linkBuilder.getLinkId());
    }

    @Override
    public EventType getCreateEventType() {
        return EventType.TOPOLOGY_LINK_DISCOVERED;
    }

    @Override
    public EventType getDeleteEventType() {
        return EventType.TOPOLOGY_LINK_REMOVED;
    }

    @Override
    public EventType getUpdateEventType() {
        return EventType.TOPOLOGY_LINK_UPDATED;
    }

    @Override
    public Class<?> getCreateImplClass() {
        return TopologyLinkUpImpl.class;
    }

    @Override
    public Class<?> getDeleteImplClass() {
        return TopologyLinkDeletedImpl.class;
    }

    @Override
    public Class<?> getUpdateImplClass() {
        return TopologyLinkUpdatedImpl.class;
    }

    @Override
    public void addEventListener(IEventListener<?> listener) {
        serviceRegistry.registerEventListener(this, listener);
    }

    @Override
    public void removeEventListener(IEventListener<?> listener) {
        serviceRegistry.registerEventListener(this, listener);
    }
}

