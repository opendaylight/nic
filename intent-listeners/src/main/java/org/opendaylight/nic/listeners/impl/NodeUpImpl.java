/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.NodeUp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import java.sql.Timestamp;
import java.util.Date;

public class NodeUpImpl implements NodeUp {

    private IpAddress ipAddress;
    private NodeId nodeId;
    private final Timestamp timeStamp;

    public NodeUpImpl(IpAddress ipAddress, NodeId nodeId) {
        this.ipAddress = ipAddress;
        this.nodeId = nodeId;
        Date date= new java.util.Date();
        timeStamp = new Timestamp(date.getTime());
    }

    @Override
    public IpAddress getIp() {
        return this.ipAddress;
    }

    public void setIpAddress(IpAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }

    public void setNodeId(NodeId nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public Timestamp getTimeStamp() {
        return timeStamp;
    }
}
