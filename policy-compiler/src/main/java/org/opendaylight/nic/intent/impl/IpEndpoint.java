//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent.impl;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import org.opendaylight.nic.common.Device;
import org.opendaylight.nic.common.Port;
import org.opendaylight.nic.common.SegmentId;
import org.opendaylight.nic.intent.Endpoint;
import org.opendaylight.nic.intent.EndpointAttribute;
import org.opendaylight.nic.intent.EndpointId;

/**
 * An {@link Endpoint} which represents an IP-based host on the network.
 *
 * @author Duane Mentze
 */
public class IpEndpoint implements Endpoint {

    private final EndpointId id;
    private final InetAddress ip;
    private final Port port;
    private final Device device;
    private final SegmentId segId;

    Set<EndpointAttribute> attributes;

    /**
     * Adds an attribute to this endpoint.
     * 
     * @param epa
     *            endpoint attribute
     * @return true if the attribute was added, false if it already existed
     */
    public boolean addAttribute(EndpointAttribute epa) {
        return attributes.add(epa);
    }

    /**
     * Removes an attribute from this endpoint.
     * 
     * @param epa
     *            endpoint attribute
     * @return true if the attribute was removed, false if not
     */
    public boolean removeAttribute(EndpointAttribute epa) {
        return attributes.remove(epa);
    }

    @Override
    public Set<EndpointAttribute> attributes() {
        return this.attributes;
    }

    /**
     * Returns the IP address of this endpoint. The IP address is not guaranteed
     * to be unique, except when taken in the context of a network
     * {@link SegmentId}.
     *
     * @return IP address
     */
    public InetAddress ip() {
        return this.ip;
    }

    /**
     * Returns the device port where this endpoint resides. Taken in combination
     * with the value returned by {@link #device()}, this value identifies the
     * location of the endpoint in the network.
     *
     * @return device port
     */
    public Port port() {
        return this.port;
    }

    /**
     * Returns the device where this endpoint resides. Taken in combination with
     * the value returned by {@link #port()}, this value identifies the location
     * of the endpoint in the network.
     *
     * @return device
     */
    public Device device() {
        return this.device;
    }

    @Override
    public EndpointId id() {
        return id;
    }

    /**
     * Returns the network segment for this endpoint. The network segment may be
     * a VLAN, VxLAN, or any other network segment identifier.
     *
     * @return network segment ID
     */
    public SegmentId segId() {
        return segId;
    }

    public IpEndpoint(InetAddress ip, Port port, SegmentId segId, Device device) {
        super();
        this.ip = ip;
        this.port = port;
        this.device = device;
        this.id = new IpEndpointId(ip, segId);
        this.segId = segId;
        this.attributes = new HashSet<EndpointAttribute>();
        this.attributes
                .add(new EndpointAttributeImpl(this.ip.getHostAddress()));
        this.attributes.add(new EndpointAttributeImpl(this.device.id().id()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((attributes == null) ? 0 : attributes.hashCode());
        result = prime * result + ((device == null) ? 0 : device.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        result = prime * result + ((port == null) ? 0 : port.hashCode());
        result = prime * result + ((segId == null) ? 0 : segId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IpEndpoint other = (IpEndpoint) obj;
        if (attributes == null) {
            if (other.attributes != null)
                return false;
        } else if (!attributes.equals(other.attributes))
            return false;
        if (device == null) {
            if (other.device != null)
                return false;
        } else if (!device.equals(other.device))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (ip == null) {
            if (other.ip != null)
                return false;
        } else if (!ip.equals(other.ip))
            return false;
        if (port == null) {
            if (other.port != null)
                return false;
        } else if (!port.equals(other.port))
            return false;
        if (segId == null) {
            if (other.segId != null)
                return false;
        } else if (!segId.equals(other.segId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "IpEndpoint {ip=" + ip + ", port=" + port.value() + ", device="
                + device.id() + segId + attributes + "}";
    }

}
