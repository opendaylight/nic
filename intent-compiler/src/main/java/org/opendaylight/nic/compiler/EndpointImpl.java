/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.compiler;

import org.opendaylight.nic.compiler.api.Endpoint;

import java.net.InetAddress;

public class EndpointImpl implements Endpoint {
    InetAddress ipAddress;

    public EndpointImpl(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        EndpointImpl endpoint = (EndpointImpl) object;

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
