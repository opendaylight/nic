/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.utils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.LinkKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.base.Preconditions;

public class IidFactory {

    /**
     * Method returns a wildCard {@link InstanceIdentifier} for {@link Node} from inventory
     * because this path is a base for every OF paths.
     *
     * @return WildCarded InstanceIdentifieis a base for every OF paths.r for Node
     */
    public static InstanceIdentifier<Node> getNodeWildII() {
        return InstanceIdentifier.create(Nodes.class).child(Node.class);
    }

    /**
     * Method returns an {@link InstanceIdentifier} for {@link Node} from inventory
     * because this path is a base for every OF paths.
     *
     * @param ii - key for keyed {@link Node} {@link InstanceIdentifier}
     * @return InstanceIdentifier for Node
     */
    public static InstanceIdentifier<Node> getNodeII(final InstanceIdentifier<?> ii) {
        final NodeKey key = ii.firstKeyOf(Node.class);
        Preconditions.checkArgument(key != null);
        final InstanceIdentifier<Node> nodeIid = InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, key)
                .build();
        return nodeIid;
    }

    /**
     * @param path pointer to element
     * @return extracted {@link NodeKey} and wrapped in {@link NodeRef}
     */
    public static NodeRef createNodeRef(InstanceIdentifier<?> path) {
        final InstanceIdentifier<Node> nodePath = Preconditions.checkNotNull(path.firstIdentifierOf(Node.class));
        return new NodeRef(nodePath);
    }

    /**
     * @param path pointer to element
     * @return extracted {@link NodeId}
     */
    public static NodeId getNodeId(InstanceIdentifier<?> path) {
        final NodeKey nodeKey = Preconditions.checkNotNull(path.firstKeyOf(Node.class));
        return nodeKey.getId();
    }

    /**
     * Method returns a wildCard {@link InstanceIdentifier} for {@link Link} from
     * Network-Topology because this path is a base for every OF paths.
     *
     * @return WildCarded InstanceIdentifier for Link
     */
    public static InstanceIdentifier<Link> getLinkWildII() {
        return InstanceIdentifier.create(NetworkTopology.class)
                                 .child(Topology.class)
                                 .child(Link.class);
    }

    /**
     * Method returns an {@link InstanceIdentifier} for {@link Link} from
     * Network-Topology because this path is updated within the OF
     * project.
     *
     * @param path key for keyed {@link Link} {@link InstanceIdentifier}
     * @return InstanceIdentifier for Link
     */
    public static InstanceIdentifier<Link> getLinkII(final InstanceIdentifier<?> path) {
        final LinkKey key = path.firstKeyOf(Link.class);
        Preconditions.checkArgument(key != null);
        final InstanceIdentifier<Link> link = InstanceIdentifier
                                                  .builder(NetworkTopology.class)
                                                  .child(Topology.class)
                                                  .child(Link.class, key)
                                                  .build();
        return link;
    }
}
