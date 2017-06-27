/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.common.transaction.service.renderer;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.Dataflow;

/**
 * Service for OpenFlow renderer
 */
public interface OFRendererService extends RendererService<Dataflow> {

    /**
     * Create LLDP flows using the OpenFlow renderer
     * @param nodeId the {@link NodeId}
     */
    void evaluateLLDPFlow(NodeId nodeId);

    /**
     * Create ARP flows using the OpenFlow renderer
     * @param nodeId the {@link NodeId}
     */
    void evaluateArpFlows(NodeId nodeId);

    /**
     * Create flows based on a given {@link Intent}
     * @param intent the {@link Intent}
     */
    void applyIntent(Intent intent);

    /**
     * Remove flows based on a given {@link Intent}
     * @param intent the {@link Intent}
     */
    void removeIntent(Intent intent);
}
