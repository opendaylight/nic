/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.utils.ArpOperation;
import org.opendaylight.nic.listeners.utils.Arp;
import org.opendaylight.nic.listeners.utils.ArpResolverUtils;
import org.opendaylight.nic.listeners.utils.ArpUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointResolver implements PacketProcessingListener {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointResolver.class);

    @Override
    public void onPacketReceived(PacketReceived potentialArp) {
        Arp arp = null;
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
            //TODO: add to mapping service
        }
        if (arp.getOperation() == ArpOperation.REPLY.intValue()) {
            LOG.trace("ARP REPLY packet received - {}", ArpUtils.getArpToStringFormat(arp));
            Ipv4Address sourceIp = ArpUtils.bytesToIp(arp.getSenderProtocolAddress());
            MacAddress  sourceMac = ArpUtils.bytesToMac(arp.getSenderHardwareAddress());
            Ipv4Address destIp = ArpUtils.bytesToIp(arp.getTargetProtocolAddress());
            MacAddress destMac = ArpUtils.bytesToMac(arp.getTargetHardwareAddress());
            //TODO: add to mapping service
        }
    }
}
