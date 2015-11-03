/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.LinkUp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;

public class LinkUpImpl implements LinkUp {
    private MacAddress mac = null;
    private String portName = null;
    private NodeConnectorId nodeConnectorId = null;

    public LinkUpImpl(MacAddress mac, String portName,
               NodeConnectorId nodeConnectorId) {
        this.mac = mac;
        this.portName = portName;
        this.nodeConnectorId = nodeConnectorId;
    }
    @Override
    public void setMac(MacAddress mac) {
        this.mac = mac;
    }

    @Override
    public MacAddress getMac() {
        return this.mac;
    }

    @Override
    public void setPortName(String portName) {
        this.portName = portName;
    }

    @Override
    public String getPortName() {
        return this.portName;
    }

    @Override
    public void setNodeConnectorId(NodeConnectorId nodeConnectorId) {
        this.nodeConnectorId = nodeConnectorId;
    }

    @Override
    public NodeConnectorId getNodeConnectorId() {
        return this.nodeConnectorId;
    }
}
