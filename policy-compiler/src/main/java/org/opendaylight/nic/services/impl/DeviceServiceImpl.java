//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.services.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opendaylight.nic.common.Device;
import org.opendaylight.nic.common.DeviceId;
import org.opendaylight.nic.services.DeviceService;


public class DeviceServiceImpl implements DeviceService {

    /** added devices */
    private final Map<DeviceId,Device> devices;
    
    public Map<DeviceId,Device> getDevices() {
    	return devices;
    }

    public DeviceServiceImpl() {
        devices = new HashMap<>();
    }

    @Override
    public void add(Device device) {
        if (devices.get(device) != null)
            throw new IllegalArgumentException("Device already added: " + device);

        devices.put(device.id(), device);
    }


    @Override
    public void remove(Device device) {
        devices.remove(device);
    }

    @Override
    public Device get(DeviceId id) {
        return devices.get(id);
    }

    @Override
    public Map<DeviceId, Device> getAll() {
        return Collections.unmodifiableMap(devices);
    }

}
