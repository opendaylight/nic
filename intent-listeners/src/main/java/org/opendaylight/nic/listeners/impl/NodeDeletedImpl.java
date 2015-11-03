/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.NodeDeleted;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;

public class NodeDeletedImpl implements NodeDeleted {
    private NodeRef nodeRef;

    public NodeDeletedImpl(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }

    @Override
    public NodeRef getNodeRef() {
        return this.nodeRef;
    }

    public void setIpAddress(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
    }
}
