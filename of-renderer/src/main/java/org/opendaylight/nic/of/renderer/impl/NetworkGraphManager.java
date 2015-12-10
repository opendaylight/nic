/*
 * Copyright (c) 2015 Inocybe inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendaylight.nic.of.renderer.api.OFRenderedGraphService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class NetworkGraphManager implements OFRenderedGraphService {

    private Graph<NodeId, Link> networkGraph;
    Set<String> linkAdded = new HashSet<>();
    private DijkstraShortestPath<NodeId, Link> shortestPath;

    public NetworkGraphManager() {
        networkGraph = new SparseMultigraph<>();
        shortestPath = new DijkstraShortestPath<>(networkGraph);
    }

    @Override
    public Graph<NodeId, Link> getGraph() {
        return this.networkGraph;
    }

    @Override
    public List<Link> getShortestPath(NodeId source, NodeId target) {
        return shortestPath.getPath(source, target);
    }

    @Override
    public synchronized void setLinks(List<Link> links) {
        if (links != null && !links.isEmpty()) {
            for(Link link : links) {
                if(linkAlreadyAdded(link)) {
                  continue;
                }
                NodeId sourceNodeId = link.getSource().getSourceNode();
                NodeId destinationNodeId = link.getDestination().getDestNode();
                networkGraph.addVertex(sourceNodeId);
                networkGraph.addVertex(destinationNodeId);
                networkGraph.addEdge(link, sourceNodeId, destinationNodeId, EdgeType.UNDIRECTED);
              }
        }
    }

    protected boolean linkAlreadyAdded(Link link) {
        String linkAddedKey = null;
        if(link.getDestination().getDestTp().hashCode() > link.getSource().getSourceTp().hashCode()) {
          linkAddedKey = link.getSource().getSourceTp().getValue() + link.getDestination().getDestTp().getValue();
        } else {
          linkAddedKey = link.getDestination().getDestTp().getValue() + link.getSource().getSourceTp().getValue();
        }
        if(linkAdded.contains(linkAddedKey)) {
          return true;
        } else {
          linkAdded.add(linkAddedKey);
          return false;
        }
    }

}
