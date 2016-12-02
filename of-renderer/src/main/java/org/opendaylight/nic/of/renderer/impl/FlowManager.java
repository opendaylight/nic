/*
 * Copyright (c) 2016 Open Networking Foundation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.impl;

import org.apache.commons.lang3.NotImplementedException;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.common.model.FlowData;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

/**
 * Created by icarocamelo on 2016-12-01.
 */
public class FlowManager extends AbstractFlowManager {
    FlowManager(DataBroker dataBroker, PipelineManager pipelineManager) {
        super(dataBroker, pipelineManager);
    }

    @Override
    protected String createFlowName() {
        return null;
    }

    @Override
    void pushFlow(NodeId nodeId, FlowAction flowAction) {
        throw new NotImplementedException("deprecated");
    }

    @Override
    void pushFlow(FlowData flowData) {
        //TODO: get information from renderer
    }
}
