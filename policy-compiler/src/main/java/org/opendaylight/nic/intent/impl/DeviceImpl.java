//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent.impl;

import org.opendaylight.nic.common.Device;
import org.opendaylight.nic.common.DeviceId;

/**
 * @author Duane Mentze
 */
public class DeviceImpl implements Device {

    private final DeviceId id;

    public DeviceImpl(DeviceId id) {
        if (id == null)
            throw new IllegalArgumentException("Required arguments cannot be null");

        this.id = id;
    }

	@Override
	public DeviceId id() {
		return id;
	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
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
        DeviceImpl other = (DeviceImpl) obj;
        if (!id.equals(other.id))
            return false;
        return true;
    }

	@Override
	public String toString() {
		return "OdlDeviceImpl [name=" + id + "]";
	}


}
