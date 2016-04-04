/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import common.RendererFlowModel;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yrineu on 28/03/16.
 */
public class GraphFlowManager extends AbstractFlowManager {

    private final AtomicLong flowCookie = new AtomicLong();

    GraphFlowManager(DataBroker dataBroker, PipelineManager pipelineManager) {
        super(dataBroker, pipelineManager);
    }

    @Override
    protected String createFlowName() {
        StringBuilder graphFlowName = new StringBuilder();
        graphFlowName.append(OFRendererConstants.GRAPH_FLOW_NAME);
        graphFlowName.append(flowCookie.get());
        return graphFlowName.toString();
    }

    @Override
    void pushFlow(NodeId nodeId, FlowAction flowAction) {
        //TODO: Do Nothing, change the AbstractFlowManager
    }

    void pushFlow(RendererFlowModel rendererFlowModel) {
        //TODO: Provide a Graph push flow here
    }
}
