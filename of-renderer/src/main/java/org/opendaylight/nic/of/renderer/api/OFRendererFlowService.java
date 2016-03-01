/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.api;

import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

public interface OFRendererFlowService {

    /**
     * Push OF rules to add an Intent
     * @param intent The {@link Intent} that has been created
     * @param flowAction The {@link FlowAction} (Add or Remove)
     */
    void pushIntentFlow(Intent intent, FlowAction flowAction);

    /**
     * Push ARP flows on node-up event
     * @param nodeId The OF {@link NodeId}
     * @param flowAction The {@link FlowAction}
     */
    void pushARPFlow(NodeId nodeId, FlowAction flowAction);

    /**
     * Push OF rules to forward LLDP packets to controller
     * @param nodeId The OF {@link NodeId}
     * @param flowAction The {@link FlowAction}
     */
    void pushLLDPFlow(NodeId nodeId, FlowAction flowAction);

    String getShortestPath(String srcIP, String dstIP);

    String getDisjointPaths(String srcIP, String dstIP);
}
