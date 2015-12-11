/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Nodes;

import java.net.InetAddress;

public class NodeImpl {
    /**
     * Class of Node implementation to form a label-tree of high-level strings or low-level IP addresses
     **/
    // TODO: Extend this class to create label tree

    protected InetAddress ipAddress;
    protected Nodes macAddress;
    protected int port;

    public NodeImpl(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public NodeImpl(Nodes macAddress) {
        this.macAddress = macAddress;
    }

    public NodeImpl(int port) {
        this.port = port;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public Nodes getMacAddress() {
        return macAddress;
    }

    public int getPortAddress() {
        return port;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        NodeImpl endpoint = (NodeImpl) object;

        return !(ipAddress != null ? !ipAddress.equals(endpoint.ipAddress) : endpoint.ipAddress != null);

    }

    @Override
    public int hashCode() {
        return ipAddress != null ? ipAddress.hashCode() : 0;
    }

    @Override
    public String toString() {
        return ipAddress.getHostAddress();
    }
}
