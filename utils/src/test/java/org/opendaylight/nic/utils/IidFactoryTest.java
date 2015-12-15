/*
 * Copyright (c) 2015 Inocybe inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.utils;

import static org.mockito.Mockito.mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkKey;


public class IidFactoryTest {

    @Test
    public void getNodeWildIITest() throws Exception {
        assertTrue(IidFactory.getLinkWildII() instanceof InstanceIdentifier);
    }

    @Test
    public void getNodeIITest() throws Exception {
        NodeKey mockNodeKey = mock(NodeKey.class);
        InstanceIdentifier<?> iid = InstanceIdentifier.builder(Nodes.class).child(Node.class, mockNodeKey).build();
        assertTrue(IidFactory.getNodeII(iid) instanceof InstanceIdentifier);
    }

    @Test
    public void createNodeRefTest() throws Exception {
        NodeKey mockNodeKey = mock(NodeKey.class);
        InstanceIdentifier<?> iid = InstanceIdentifier.builder(Nodes.class).child(Node.class, mockNodeKey).build();
        NodeRef nodeRef = new NodeRef(iid);
        assertEquals(nodeRef, IidFactory.createNodeRef(iid));
    }

    @Test
    public void getNodeIdTest() throws Exception {
        NodeKey mockNodeKey = mock(NodeKey.class);
        InstanceIdentifier<?> iid = InstanceIdentifier.builder(Nodes.class).child(Node.class, mockNodeKey).build();
        assertEquals(mockNodeKey.getId(), IidFactory.getNodeId(iid));
    }

    @Test
    public void getLinkWildIITest() throws Exception {
        assertTrue(IidFactory.getLinkWildII() instanceof InstanceIdentifier);
    }

    @Test
    public void getLinkIITest() throws Exception {
        LinkKey mockLinkKey = mock(LinkKey.class);
        InstanceIdentifier<?> iid = InstanceIdentifier
                                        .builder(NetworkTopology.class)
                                        .child(Topology.class)
                                        .child(Link.class, mockLinkKey)
                                        .build();
        assertTrue(IidFactory.getLinkII(iid) instanceof InstanceIdentifier);
    }
}
