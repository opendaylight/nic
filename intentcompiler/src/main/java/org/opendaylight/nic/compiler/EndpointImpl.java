//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import org.opendaylight.nic.compiler.api.*;

import java.net.InetAddress;

public class EndpointImpl implements Endpoint {
    
    public InetAddress address;
    
    @Override
    public InetAddress getIpAddress() {
	
	return address;
    }
    
    public EndpointImpl(InetAddress ad) {
	
	this.address=ad;
	
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EndpointImpl endpoint = (EndpointImpl) o;

        return !(address != null ? !address.equals(endpoint.address) : endpoint.address != null);

    }

    @Override
    public int hashCode() {
        return address != null ? address.hashCode() : 0;
    }

    @Override
    public String toString() {
        return address.getHostAddress();
    }
 
    
    
}