//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.common;

/**
 * A network device (such as a switch) which is controlled by our controller.
 *
 * @author Shaun Wackerly
 */
public interface Device {

    /**
     * Returns the globally-unique identifier for this device.
     *
     * @return device ID
     */
    DeviceId id();

}
