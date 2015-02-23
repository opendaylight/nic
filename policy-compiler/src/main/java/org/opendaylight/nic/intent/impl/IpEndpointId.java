//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent.impl;

import java.net.InetAddress;

import org.opendaylight.nic.common.SegmentId;
import org.opendaylight.nic.intent.EndpointId;

/**
 * A globally-unique identifier for a specific endpoint, where the
 * identifier is based upon the unique combination of IP address
 * and network segment.
 *
 * @author Shaun Wackerly
 */
public class IpEndpointId implements EndpointId {

    /** IP address of the endpoint. */
    private final InetAddress ip;

    /** Network segment ID of the endpoint. */
    private final SegmentId segId;

    /**
     * Constructs an endpoint ID which represents the endpoint
     * with the given IP on the given network segment.
     *
     * @param ip IP address
     * @param segId network segment
     */
    public IpEndpointId(InetAddress ip, SegmentId segId) {
        if (ip == null || segId == null)
            throw new IllegalArgumentException("Required arguments cannot be null");

        this.ip = ip;
        this.segId = segId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ip.hashCode();
        result = prime * result + segId.hashCode();
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
        IpEndpointId other = (IpEndpointId) obj;
        if (!ip.equals(other.ip))
            return false;
        if (!segId.equals(other.segId))
            return false;
        return true;
    }

	@Override
	public String toString() {
		return segId.toString()+":"+ip.getHostAddress();
	}
}
