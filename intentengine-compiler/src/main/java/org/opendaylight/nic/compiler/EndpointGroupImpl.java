//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

public class EndpointGroupImpl implements EndpointGroup {

    private final String group;

    @Override
    public String group() {
        return group;

    }

    public EndpointGroupImpl(String group) {
        this.group = group;
    }

    public EndpointGroupImpl and(EndpointGroupImpl other) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(String.format("( %s ) and ( %s) )", group, other.group()));
        return new EndpointGroupImpl(new String(buffer));
    }

    public EndpointGroupImpl andNot(EndpointGroupImpl other) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(String.format("( %s ) and not ( %s) )", group,
                other.group()));
        return new EndpointGroupImpl(new String(buffer));
    }

    @Override
    public String toString() {
        return group;
    }

    @Override
    public int hashCode() {
        return group.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EndpointGroup other = (EndpointGroup) obj;
        return group.equals(other.group());
    }
}