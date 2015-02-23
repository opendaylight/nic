//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.services;

import java.util.Map;

import org.opendaylight.nic.common.Device;
import org.opendaylight.nic.common.DeviceId;

/**
 * Provides a mechanism for management of devices. Devices are connected
 * to an managed by the controller.
 *
 * @author Duane Mentze
 */
public interface DeviceService {

    /**
     * Adds the given device as available for use.
     *
     * @param device to be added
     */
    void add(Device device);

    /**
     * The given device is removed and no longer available for use.
     *
     * @param device to be removed
     */
    void remove(Device device);

    /**
     * Gets the device with the specified ID.
     *
     * @param id device ID
     * @return device, or null if not found
     */
    Device get(DeviceId id);

    /**
     * Gets all devices.
     *
     * @return all devices
     */
    Map<DeviceId,Device> getAll();
}

