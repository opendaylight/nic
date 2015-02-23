//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.common;

/**
 * A class which uniquely identifies a specific device in a global context.
 * This class is mainly used as a temporary way of mapping to ODL structures.
 */
public class DeviceId {

	public String id() {
        return id;
    }


    private final String id;

	/**
	 * Constructs an ODL device ID.
	 *
	 * @param id device ID
	 */
    public DeviceId(String id) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");
        this.id = id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DeviceId other = (DeviceId) obj;
        return id.equals(other.id);
    }


    @Override
	public String toString() {
		return "OdlDeviceId [id=" + id + "]";
	}
}
