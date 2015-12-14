/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opendaylight.controller.liblldp.EtherTypes;
import org.opendaylight.controller.liblldp.Ethernet;
import org.opendaylight.controller.liblldp.HexEncode;
import org.opendaylight.controller.liblldp.Packet;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.InetAddresses;

public class ArpUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ArpUtils.class);

    private ArpUtils() {
        throw new UnsupportedOperationException("Cannot create an instance.");
    }

    /**
     * Returns Ethernet and ARP in readable string format
     * @param eth The {@link Ethernet} packet representation
     * @return {@link String} that represents Ethernet and ARP
     */
    public static String getArpFrameToStringFormat(Ethernet eth) {
        String ethernetString = "Ethernet [getEtherType()="
                + EtherTypes.loadFromString(String.valueOf(eth.getEtherType())) + ", getSourceMACAddress()="
                + HexEncode.bytesToHexStringFormat(eth.getSourceMACAddress()) + ", getDestinationMACAddress()="
                + HexEncode.bytesToHexStringFormat(eth.getDestinationMACAddress()) + "]\n";
        Packet potentialArp = eth.getPayload();
        String arpString = null;
        if (potentialArp instanceof Arp) {
            Arp arp = (Arp) potentialArp;
            arpString = ArpUtils.getArpToStringFormat(arp);
        } else {
            arpString = "ARP was not found in Ethernet frame.";
        }
        return ethernetString.concat(arpString);
    }

    /**
     * Returns ARP in readable string format
     * @param arp The {@link Arp} packet representation
     * @return {@link String} that represents an ARP packet
     */
    public static String getArpToStringFormat(Arp arp) {
        try {
            return "Arp [getHardwareType()=" + arp.getHardwareType() + ", getProtocolType()=" + arp.getProtocolType()
                    + ", getHardwareLength()=" + arp.getHardwareLength() + ", getProtocolLength()="
                    + arp.getProtocolLength() + ", getOperation()=" + ArpOperation.loadFromInt(arp.getOperation())
                    + ", getSenderHardwareAddress()="
                    + HexEncode.bytesToHexStringFormat(arp.getSenderHardwareAddress())
                    + ", getSenderProtocolAddress()="
                    + InetAddress.getByAddress(arp.getSenderProtocolAddress()).getHostAddress()
                    + ", getTargetHardwareAddress()="
                    + HexEncode.bytesToHexStringFormat(arp.getTargetHardwareAddress())
                    + ", getTargetProtocolAddress()="
                    + InetAddress.getByAddress(arp.getTargetProtocolAddress()).getHostAddress() + "]\n";
        } catch (UnknownHostException e1) {
            LOG.error("Error during parsing Arp {}", arp, e1);
            return null;
        }
    }

    /**
     * Converts a {@link MacAddress} object into a byte array.
     * @param mac {@link MacAddress} Mac Address Object
     * @return mac as a byte array
     */
    public static byte[] macToBytes(MacAddress mac) {
        return HexEncode.bytesFromHexString(mac.getValue());
    }

    /**
     * Converts a byte array representing a mac address into
     * an object {@link MacAddress}.
     * @param macBytes Byte array representing the mac address.
     * @return mac as a {@link MacAddress} object
     */
    public static MacAddress bytesToMac(byte[] macBytes) {
        String mac = HexEncode.bytesToHexStringFormat(macBytes);
        if (!"null".equals(mac)) {
            return new MacAddress(mac);
        }
        return null;
    }

    /**
     * Converts an {@link Ipv4Address} object to a byte array.
     * @param ip {@link Ipv4Address} IPv4 object
     * @return ip as a byte array
     */
    public static byte[] ipToBytes(Ipv4Address ip) {
        return InetAddresses.forString(ip.getValue()).getAddress();
    }

    /**
     * Converts a byte array to a {@link Ipv4Address} object.
     * @param ipv4AsBytes byte array
     * @return ip as a {@link Ipv4Address} object
     */
    public static Ipv4Address bytesToIp(byte[] ipv4AsBytes) {
        try {
            return new Ipv4Address(InetAddress.getByAddress(ipv4AsBytes).getHostAddress());
        } catch (UnknownHostException e) {
            LOG.error("Failed to convert bytes to IP", e);
            return null;
        }
    }

}