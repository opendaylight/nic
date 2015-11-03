/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.EndpointDiscovered;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;

public class EndpointDiscoveredImpl implements EndpointDiscovered {
    private Ipv4Address ipv4Address;
    private MacAddress macAddress;

    public EndpointDiscoveredImpl(Ipv4Address ipv4Address, MacAddress macAddress) {
        this.ipv4Address = ipv4Address;
        this.macAddress = macAddress;
    }

    @Override
    public Ipv4Address getIp() {
        return ipv4Address;
    }

    @Override
    public void setIp(Ipv4Address ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    @Override
    public MacAddress getMac() {
        return macAddress;
    }

    @Override
    public void setMac(MacAddress macAddress) {
        this.macAddress = macAddress;
    }
}
