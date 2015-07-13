//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import java.util.Set;

import org.opendaylight.nic.compiler.api.Action;
import org.opendaylight.nic.compiler.api.Endpoint;
import org.opendaylight.nic.compiler.api.Policy;

public class PolicyImpl implements Policy {
    private Set<Endpoint> src;
    private Set<Endpoint> dst;
    private Set<Action> action;

    public PolicyImpl(Set<Endpoint> src, Set<Endpoint> dst, Set<Action> action) {
        this.src = src;
        this.dst = dst;
        this.action = action;
    }

    @Override
    public Set<Endpoint> src() {
        return src;
    }

    @Override
    public Set<Endpoint> dst() {
        return dst;
    }

    @Override
    public Set<Action> action() {
        return action;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        PolicyImpl policy = (PolicyImpl) object;

        if (src != null ? !src.equals(policy.src) : policy.src != null) {
            return false;
        }
        if (dst != null ? !dst.equals(policy.dst) : policy.dst != null) {
            return false;
        }
        if (action != null ? !action.equals(policy.action) : policy.action != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = src != null ? src.hashCode() : 0;
        result = prime * result + (dst != null ? dst.hashCode() : 0);
        result = prime * result + (action != null ? action.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("from %s to %s apply %s", src, dst, action);
    }
}
