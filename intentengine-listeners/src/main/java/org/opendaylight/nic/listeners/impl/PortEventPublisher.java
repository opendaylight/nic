/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.IEventService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.OpendaylightInventoryListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeUpdated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PortEventPublisher implements OpendaylightInventoryListener, IEventService {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyEventPublisher.class);
    private static EventServiceRegistry serviceRegistry = EventServiceRegistry.getInstance();

    @Override
    public void onNodeConnectorRemoved(NodeConnectorRemoved notification) {
        LOG.debug("NodeConnectorRemoved Notification ...................");
        LOG.debug("NodeConnectorRef " + notification.getNodeConnectorRef());
    }

    @Override
    public void onNodeConnectorUpdated(NodeConnectorUpdated notification) {
        LOG.debug("NodeConnectorUpdated Notification...................");
        LOG.debug("NodeConnectorRef " + notification.getNodeConnectorRef());
    }

    @Override
    public void onNodeRemoved(NodeRemoved notification) {
        LOG.debug("NodeRemoved Notification ...................");
        LOG.debug("NodeRef " + notification.getNodeRef());
        notifyEventListener(EventType.NODEREMOVED);
    }

    @Override
    public void onNodeUpdated(NodeUpdated notification) {
        LOG.debug("NodeUpdated Notification ...................");
        LOG.debug("NodeRef " + notification.getNodeRef());
        notifyEventListener(EventType.NODEUPDATED);
    }

    @Override
    public void addEventListener(IEventListener listener) {
        serviceRegistry.registerEventListener(listener);
    }

    @Override
    public void removeEventListener(IEventListener listener) {
        serviceRegistry.unregisterEventListener(listener);
    }

    @Override
    public void notifyEventListener(EventType eventType) {
        serviceRegistry.notifyEvent(eventType);
    }
}