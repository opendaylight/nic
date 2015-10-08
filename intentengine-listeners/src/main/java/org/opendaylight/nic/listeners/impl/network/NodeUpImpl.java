/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl.network;

import org.opendaylight.nic.listeners.api.network.NodeUp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;

public class NodeUpImpl implements NodeUp {

    private IpAddress ipAddress;

    NodeUpImpl() {

    }

    NodeUpImpl(IpAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public IpAddress getIp() {
        return this.ipAddress;
    }

    public void setIpAddress(IpAddress ipAddress) {
        this.ipAddress = ipAddress;
    }
}
