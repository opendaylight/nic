/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.nic.listeners.api.EndpointDiscovered;
import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.IEventService;
import org.opendaylight.nic.listeners.utils.ArpOperation;
import org.opendaylight.nic.listeners.utils.Arp;
import org.opendaylight.nic.listeners.utils.ArpResolverUtils;
import org.opendaylight.nic.listeners.utils.ArpUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class EndpointDiscoveredNotificationSupplierImpl implements PacketProcessingListener, IEventService {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointDiscoveredNotificationSupplierImpl.class);
    private static EventServiceRegistry serviceRegistry = EventServiceRegistry.getInstance();
    private ListenerRegistration<EndpointDiscoveredNotificationSupplierImpl> notificationListenerRegistration = null;

    public EndpointDiscoveredNotificationSupplierImpl(NotificationService notificationService) {
        serviceRegistry.setEventTypeService(this, EventType.ENDPOINT_DISCOVERED);
        notificationListenerRegistration = notificationService.registerNotificationListener(this);
    }

    @Override
    public void onPacketReceived(PacketReceived potentialArp) {
        Arp arp = null;
        EndpointDiscovered sourceEndpointDiscovered = null;
        EndpointDiscovered destEndpointDiscovered = null;
        try {
            arp = ArpResolverUtils.getArpFrom(potentialArp);
        } catch (Exception e) {
            LOG.trace(
                    "Failed to decode potential ARP packet. This could occur when other than ARP packet was received",
                    e);
            return;
        }
        if (arp.getOperation() == ArpOperation.REQUEST.intValue()) {
            LOG.trace("ARP REQUEST packet received - {}", ArpUtils.getArpToStringFormat(arp));
            Ipv4Address sourceIp = ArpUtils.bytesToIp(arp.getSenderProtocolAddress());
            MacAddress  sourceMac = ArpUtils.bytesToMac(arp.getSenderHardwareAddress());
            sourceEndpointDiscovered = new EndpointDiscoveredImpl(sourceIp,sourceMac);
        }
        if (arp.getOperation() == ArpOperation.REPLY.intValue()) {
            LOG.trace("ARP REPLY packet received - {}", ArpUtils.getArpToStringFormat(arp));
            Ipv4Address sourceIp = ArpUtils.bytesToIp(arp.getSenderProtocolAddress());
            MacAddress  sourceMac = ArpUtils.bytesToMac(arp.getSenderHardwareAddress());
            Ipv4Address destIp = ArpUtils.bytesToIp(arp.getTargetProtocolAddress());
            MacAddress destMac = ArpUtils.bytesToMac(arp.getTargetHardwareAddress());
            sourceEndpointDiscovered = new EndpointDiscoveredImpl(sourceIp, sourceMac);
            destEndpointDiscovered = new EndpointDiscoveredImpl(destIp, destMac);
        }
        Set<IEventListener> eventListeners =
                serviceRegistry.getEventListeners(EventType.ENDPOINT_DISCOVERED);
        if (eventListeners != null) {
            for (IEventListener listener : eventListeners) {
                if (sourceEndpointDiscovered != null) {
                    listener.handleEvent(sourceEndpointDiscovered);
                }
                if (destEndpointDiscovered != null) {
                    listener.handleEvent(destEndpointDiscovered);
                }
            }
        }
    }

    @Override
    public void addEventListener(IEventListener listener) {
        serviceRegistry.registerEventListener(this, listener);
    }

    @Override
    public void removeEventListener(IEventListener listener) {
        serviceRegistry.unregisterEventListener(this, listener);
    }

    public void close() {
        if (notificationListenerRegistration != null) {
            notificationListenerRegistration.close();
        }
    }
}
