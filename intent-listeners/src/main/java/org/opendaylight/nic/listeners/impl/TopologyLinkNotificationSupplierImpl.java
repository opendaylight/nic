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
import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.nic.listeners.api.TopologyLinkDeleted;
import org.opendaylight.nic.listeners.api.TopologyLinkUp;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class TopologyLinkNotificationSupplierImpl extends
        AbstractNotificationSupplierItemRoot<Link, TopologyLinkUp, TopologyLinkDeleted, NicNotification> {

    private static final InstanceIdentifier<Link> LINK_IID = getLinkWildII();

    public TopologyLinkNotificationSupplierImpl(final DataBroker db) {
        super(db, Link.class, LogicalDatastoreType.OPERATIONAL);
    }

    @Override
    public InstanceIdentifier<Link> getWildCardPath() {
        return LINK_IID;
    }

    @Override
    public TopologyLinkUp createNotification(final Link object,
                                             final InstanceIdentifier<Link> path) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(path != null);
        //TODO: implement the builder for this notififcation
        LinkId linkId = path.firstKeyOf(Link.class).getLinkId();
        return new TopologyLinkUpImpl(object, linkId);
    }

    @Override
    public TopologyLinkDeleted deleteNotification(final Link link,
                                                  final InstanceIdentifier<Link> path) {
        Preconditions.checkArgument(path != null);
        //TODO: implement the builder for this notififcation
        return new TopologyLinkDeletedImpl(link);
    }

    @Override
    public NicNotification updateNotification(Link object, InstanceIdentifier<Link> path) {
        //Do nothing
        return null;
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
        //Do nothing
        return null;
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
        //Do nothing
        return null;
    }
}

