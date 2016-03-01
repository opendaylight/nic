/*
 * Copyright (c) 2015 - 2016 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.utils;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yrineu on 29/02/16.
 */
public class TopologyUtils {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyUtils.class);

    /**
     * Derivate a {@link org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId}
     * from a {@link NodeConnector}.
     * @param nodeConnectorId The Inventory NodeConnectorId
     * @return NodeId contained in Network-Topology
     */
    public static org.opendaylight.yang.gen.v1.urn
            .tbd.params.xml.ns.yang.network
            .topology.rev131021.NodeId extractTopologyNodeId(String nodeConnectorId) {
        List<String> split = Arrays.asList(nodeConnectorId.split(":"));
        return new org.opendaylight.yang.gen.v1.urn
                .tbd.params.xml.ns.yang.network
                .topology.rev131021.NodeId(split.get(0) +
                ":" +
                split.get(1));
    }

    /**
     * Retrieve all the {@link Node} along with a list of their
     * associated {@link NodeConnector}.
     * @return nodes A map with {@link Node} as the key and a {@link List} of {@link NodeConnector}.
     */
    public static Map<Node, List<NodeConnector>> getNodes(final DataBroker dataBroker) {
        Map<Node, List<NodeConnector>> nodeMap = new HashMap<Node, List<NodeConnector>>();
        Nodes nodeList = new NodesBuilder().build();
        ReadTransaction tx = dataBroker.newReadOnlyTransaction();
        try {
            final InstanceIdentifier<Nodes> nodesIdentifier = InstanceIdentifier.create(Nodes.class);
            final CheckedFuture<Optional<Nodes>, ReadFailedException> txCheckedFuture = tx.read(LogicalDatastoreType
                    .OPERATIONAL, nodesIdentifier);
            nodeList = txCheckedFuture.checkedGet().get();

            for (Node node : nodeList.getNode()) {
                LOG.info("Node ID : {}", node.getId());
                List<NodeConnector> nodeConnector = node.getNodeConnector();
                nodeMap.put(node, nodeConnector);
            }
        } catch (ReadFailedException e) {
            //TODO: Perform fail over
            LOG.error("Error reading Nodes from MD-SAL", e);
        }
        return nodeMap;
    }
}
