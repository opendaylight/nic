/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;

import java.sql.Timestamp;
import java.util.Date;

public class NodeUpdatedImpl implements NodeUpdated {

    private NodeRef nodeRef;
    private final Timestamp timeStamp;

    public NodeUpdatedImpl(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
        Date date= new Date();
        timeStamp = new Timestamp(date.getTime());
    }

    @Override
    public NodeRef getNodeRef() {
        return getNodeRef();
    }

    @Override
    public Timestamp getTimeStamp() {
        return timeStamp;
    }
}
