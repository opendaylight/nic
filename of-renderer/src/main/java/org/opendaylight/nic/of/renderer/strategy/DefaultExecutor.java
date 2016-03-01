/*
 * Copyright (c) 2015 - 2016 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.strategy;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.of.renderer.impl.IntentFlowManager;
import org.opendaylight.nic.of.renderer.utils.TopologyUtils;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;

import java.util.List;
import java.util.Map;

/**
 * Created by yrineu on 01/03/16.
 */
public class DefaultExecutor implements ActionStrategy {

    private final IntentFlowManager intentFlowManager;
    private final DataBroker dataBroker;

    public DefaultExecutor(final IntentFlowManager intentFlowManager, final DataBroker dataBroker) {
        this.intentFlowManager = intentFlowManager;
        this.dataBroker = dataBroker;
    }

    @Override
    public void execute(Intent intent, FlowAction flowAction) {
        final List<String> endPointGroups = IntentUtils.extractEndPointGroup(intent);
        final Action actionContainer = IntentUtils.getAction(intent);

        intentFlowManager.setEndPointGroups(endPointGroups);
        intentFlowManager.setAction(actionContainer);
        intentFlowManager.setIntent(intent);
        //Get all node Id's
        final Map<Node, List<NodeConnector>> nodeMap = TopologyUtils.getNodes(dataBroker);
        for (Map.Entry<Node, List<NodeConnector>> entry : nodeMap.entrySet()) {
            //Push flow to every node for now
            intentFlowManager.pushFlow(entry.getKey().getId(), flowAction);
        }
    }
}
