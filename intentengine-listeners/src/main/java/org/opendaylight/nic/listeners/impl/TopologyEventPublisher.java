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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.FlowTopologyDiscoveryListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkDiscovered;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkOverutilized;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.topology.discovery.rev130819.LinkUtilizationNormal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Override;

class TopologyEventPublisher implements FlowTopologyDiscoveryListener, IEventService {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyEventPublisher.class);
    private static EventServiceRegistry serviceRegistry = EventServiceRegistry.getInstance();

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

    @Override
    public void onLinkDiscovered(LinkDiscovered notification) {
        LOG.debug("LinkDiscovered notification ........");
        notifyEventListener(EventType.LINKDISCOVERED);
    }

    @Override
    public void onLinkOverutilized(LinkOverutilized notification) {
        LOG.debug("LinkOverutilized notification ........");
        notifyEventListener(EventType.LINKOVERUTILIZED);
    }

    @Override
    public void onLinkRemoved(LinkRemoved notification) {
        LOG.debug("LinkRemoved notification   ........");
        notifyEventListener(EventType.LINKREMOVED);
    }

    @Override
    public void onLinkUtilizationNormal(LinkUtilizationNormal notification) {
        LOG.debug("LinkUtilizationNormal notification ........");
        notifyEventListener(EventType.LINKUTILIZATIONNORMAL);
    }

}