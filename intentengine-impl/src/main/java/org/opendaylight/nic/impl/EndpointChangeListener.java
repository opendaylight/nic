//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointChangeListener implements DataChangeListener,
        AutoCloseable {

    private static List<Node> nodeList;
    private Map<Node, List<NodeConnector>> nodeMap;

    private static final Logger LOG = LoggerFactory
            .getLogger(EndpointChangeListener.class);
    private final DataBroker dataBroker;
    private ListenerRegistration<DataChangeListener> endpointListener = null;

    @Override
    public void close() {

    }

    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {

        createNodes(changes.getCreatedData());
        updateNodes(changes.getUpdatedData());
        deleteNodes(changes);

    }

    public EndpointChangeListener(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        InstanceIdentifier<Nodes> nodePath =
                InstanceIdentifier.create(Nodes.class);
        endpointListener =
                dataBroker.registerDataChangeListener(
                        LogicalDatastoreType.OPERATIONAL, nodePath, this,
                        DataChangeScope.SUBTREE);
    }

    public void createNodes(Map<InstanceIdentifier<?>, DataObject> changes) {

        for (Entry<InstanceIdentifier<?>, DataObject> created : changes
                .entrySet()) {

            if (created.getValue() instanceof Node) {

                Node node = (Node) created.getValue();
                nodeMap.put(node, node.getNodeConnector());

            }
        }

    }

    public void updateNodes(Map<InstanceIdentifier<?>, DataObject> changes) {

    }

    public void deleteNodes(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {

    }

    public void readInitial() {

        Nodes nodes = new NodesBuilder().build();
        ReadTransaction tx = dataBroker.newReadOnlyTransaction();
        try {
            nodeList =
                    tx.read(LogicalDatastoreType.OPERATIONAL,
                            InstanceIdentifier.create(Nodes.class))
                            .checkedGet().get().getNode();

            for (Node n : nodeList) {
                LOG.info(n.toString());
                List<NodeConnector> nodeConnector = n.getNodeConnector();
                nodeMap.put(n, nodeConnector);
            }
        } catch (ReadFailedException e) {

            e.printStackTrace();
        }

    }

    public Map<Node, List<NodeConnector>> getNodeMap() {
        return nodeMap;
    }
}