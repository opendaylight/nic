/*
 * Copyright (c) 2015 Inocybe inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.impl;

import java.util.List;

import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class NetworkGraphManager implements OFRendererGraphService {

    private Graph<NodeId, Link> networkGraph;
    private DijkstraShortestPath<NodeId, Link> shortestPath;

    public NetworkGraphManager() {
        networkGraph = new DirectedSparseMultigraph<>();
        shortestPath = new DijkstraShortestPath<>(networkGraph);
    }

    /**
     * Jung Graph Instance of a Network
     * Topology represented by Network-Topology
     * NodeId and Link.
     * @return Graph The instance of the Graph.
     */
    @Override
    public Graph<NodeId, Link> getGraph() {
        return this.networkGraph;
    }

    /**
     * Returns a list of Network-Topology Links
     * that represents the Dijkstra's algorithm
     * shortest path between a source and target
     * OF Node.
     * @param source Network-Topology Node
     * @param target Network-Topology Node
     * @return links Dijkstra Shortest Path
     */
    @Override
    public List<Link> getShortestPath(NodeId source, NodeId target) {
        return shortestPath.getPath(source, target);
    }

    /**
     * Set the Graph's links so that it
     * can be built.
     * @param links All the Network-Topology Links.
     */
    @Override
    public synchronized void setLinks(List<Link> links) {
        for (Link link: links) {
          NodeId sourceNodeId = link.getSource().getSourceNode();
          NodeId destinationNodeId = link.getDestination().getDestNode();
            if (networkGraph.findEdge(sourceNodeId, destinationNodeId) == null) {
                if (!networkGraph.containsVertex(sourceNodeId)) {
                    networkGraph.addVertex(sourceNodeId);
                }
                if (!networkGraph.containsVertex(destinationNodeId)) {
                    networkGraph.addVertex(destinationNodeId);
                }
                networkGraph.addEdge(link, sourceNodeId, destinationNodeId, EdgeType.DIRECTED);
            }
        }
    }
}
