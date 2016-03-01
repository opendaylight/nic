/*
 * Copyright (c) 2015 - 2016 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.strategy;

import org.opendaylight.nic.of.renderer.impl.QosConstraintManager;
import org.opendaylight.nic.of.renderer.utils.TopologyUtils;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;

import java.util.List;
import java.util.Map;

/**
 * Created by yrineu on 01/03/16.
 */
public class QoSExecutor implements ActionStrategy {

    private QosConstraintManager qosConstraintManager;
    private DataBroker dataBroker;

    public QoSExecutor(QosConstraintManager qosConstraintManager, DataBroker dataBroker) {
        this.qosConstraintManager = qosConstraintManager;
        this.dataBroker = dataBroker;
    }

    @Override
    public void execute(Intent intent, FlowAction flowAction) {
        //Get all node Id's
        Map<Node, List<NodeConnector>> nodeMap = TopologyUtils.getNodes(dataBroker);
        for (Map.Entry<Node, List<NodeConnector>> entry : nodeMap.entrySet()) {
            //Push flow to every node for now
            qosConstraintManager.pushFlow(entry.getKey().getId(), flowAction);
        }
    }
}
