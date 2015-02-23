//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent;

/**
 * A globally-unique identifier for a policy.
 *
 * @author Shaun Wackerly
 */
public class PolicyId {

    /** Policy name. */
    private final String name;

    /**
     * Constructs a policy ID with the given name.
     *
     * @param name policy name
     */
    public PolicyId(String name) {
        if (name == null)
            throw new IllegalArgumentException("Required arguments cannot be null");

        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
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
        PolicyId other = (PolicyId) obj;
        if (!name.equals(other.name))
            return false;
        return true;
    }

}
