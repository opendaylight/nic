/*
 * Copyright (c) 2015 Inocybe inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.api;

import java.util.List;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

import edu.uci.ics.jung.graph.Graph;

public interface OFRendererGraphService extends Subject {

    /**
     * Returns a list of Network-Topology Links
     * that represents the Dijkstra's algorithm
     * shortest path between a source and target
     * OF Node.
     * @param source Network-Topology Node
     * @param target Network-Topology Node
     * @return links Dijkstra Shortest Path
     */
    List<Link> getShortestPath(NodeId source, NodeId target);

    /**
     * Returns a list of Network-Topology Links
     * that represents the Suurbale's algorithm
     * for finding shortest pairs of disjoint paths
     * OF Node.
     * @param startVertex Network-Topology Node
     * @param endVertex Network-Topology Node
     * @return Shortest pairs of disjoint paths
     */
    List<List<Link>> getDisjointPaths(NodeId startVertex,
            NodeId endVertex);
    /**
     * Set the Graph's links so that it
     * can be built.
     * @param links All the Network-Topology Links.
     */
    void setLinks(List<Link> links);

    /**
     * Jung Graph Instance of a Network
     * Topology represented by Network-Topology
     * NodeId and Link.
     * @return Graph The instance of the Graph.
     */
    Graph<NodeId, Link> getGraph();

    /**
     * Update the Graph's links so that it can be built.
     *
     * @param links All the new Network-Topology Links.
     */
    void updateLinks(List<Link> link);

}
