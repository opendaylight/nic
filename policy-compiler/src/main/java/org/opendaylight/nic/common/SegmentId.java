//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.common;

/**
 * A network segment identifier, such as for VLAN, VxLAN, etc.
 *
 * @author Shaun Wackerly
 */
public class SegmentId {

    private final int id;

    /**
     * Constructs a network segment ID with the given value.
     *
     * @param id segment ID
     */
    public SegmentId(Integer id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
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
        SegmentId other = (SegmentId) obj;
        if (id != other.id)
            return false;
        return true;
    }

	@Override
	public String toString() {
		return Integer.valueOf(id).toString();
	}

}
