/*
 * Copyright (c) 2018 Lumina Networks.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.listener;


import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * This service is used to listen for Openflow topology events
 */
public interface TopologyListener {

    /**
     * This method will be called once a new switch comes up to the topology.
     * @param switchAdded a {@link NodeId}
     */
    void onSwitchAdd(NodeId switchAdded);

    /**
     * This method will be called once a given switch went down from the
     * topology.
     * @param switchRemoved a {@link NodeId}
     */
    void onSwitchRemoved(NodeId switchRemoved);
}
