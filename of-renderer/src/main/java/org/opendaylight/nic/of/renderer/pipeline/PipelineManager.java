/**
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.pipeline;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

public interface PipelineManager {

    /**
     * This method start the OF Pipeline services
     */
    void start();
    /**
     * Sets the table ID to the first table which supports the flow.
     * @param nodeId Node where the flow will be installed
     * @param flowBuilder Flow which will be installed
     * @return true if any suitable table has been found or false otherwise
     */
    boolean setTableId(NodeId nodeId, FlowBuilder flowBuilder);

    /**
     * Stop all Pipeline Manager services
     */
    void stop();
}
