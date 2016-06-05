/*
 * Copyright © 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.utils;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class TopologyUtilsTests {

    @Test
    public void testextractTopologyNodeId(){
        String nodeConnectorId = "openflow2:1";

        org.opendaylight.yang.gen.v1.urn
                .tbd.params.xml.ns.yang.network
                .topology.rev131021.NodeId nodeId = TopologyUtils.extractTopologyNodeId(nodeConnectorId);

        assertNotNull(nodeId);
    }

    @Test
    public void testGetNodes() throws ReadFailedException {
        List<NodeConnector> connectors = new ArrayList<>();
        List<Node> nodess = new ArrayList<>();

        NodeConnector nodeConnector = mock(NodeConnector.class);
        connectors.add(nodeConnector);

        Node node1 = mock(Node.class);
        when(node1.getNodeConnector()).thenReturn(connectors);
        nodess.add(node1);

        org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes nodes =
                mock(org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes.class);

        DataBroker dataBroker = mock(DataBroker.class);
        ReadOnlyTransaction tx = mock(ReadOnlyTransaction.class);

        CheckedFuture<Optional<DataObject>, ReadFailedException> checkedFuture = mock(CheckedFuture.class);
        Optional<DataObject> checkedGet = mock(Optional.class);

        when(nodes.getNode()).thenReturn(nodess);
        when(checkedGet.get()).thenReturn(nodes);
        when(checkedFuture.checkedGet()).thenReturn(checkedGet);
        when(tx.read(any(LogicalDatastoreType.class), any(InstanceIdentifier.class))).thenReturn(checkedFuture);
        when(dataBroker.newReadOnlyTransaction()).thenReturn(tx);

        Map<Node, List<NodeConnector>> returnedNodes = TopologyUtils.getNodes(dataBroker);

        assertTrue(returnedNodes.size() == connectors.size());
    }
}
