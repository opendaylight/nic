/*
 * Copyright (c) 2015 Inocybe inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Transformer;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.opendaylight.nic.of.renderer.api.Observer;
import org.opendaylight.nic.of.renderer.utils.SuurballeTarjanAlgorithm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class NetworkGraphManager implements OFRendererGraphService {

    private static final List<Observer> observers = new ArrayList<>();
    private static Intent message;
    private boolean changed;
    private final Object MUTEX = new Object();
    private static final Graph<NodeId, Link> networkGraph = new DirectedSparseMultigraph<>();
    private final DijkstraShortestPath<NodeId, Link> shortestPath;
    public static final List<Link> CurrentLinks = new ArrayList<>();
    public static final Map<Intent, List<List<Link>>> ProtectedLinks = new HashMap<>();

    public NetworkGraphManager() {
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
    public final List<Link> getShortestPath(NodeId source, NodeId target) {
        return shortestPath.getPath(source, target);
    }

    /**
     * Set the Graph's links so that it can be built.
     *
     * @param newlinks
     *            All the Network-Topology Links.
     */
    @Override
    public synchronized void setLinks(List<Link> newlinks) {
        CurrentLinks.clear();
        CurrentLinks.addAll(newlinks);

        for (final Link link : newlinks) {
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

    /**
     * @param changedLink
     *            Link that was updated/deleted
     * @return Identify which intents were affected
     */
    public List<Intent> getAffectedIntents(Link changedLink) {
        List<Intent> affectedIntents = new ArrayList<>();

        if (changedLink == null)
            return affectedIntents;

        for (final Intent key : ProtectedLinks.keySet()) {
            final List<List<Link>> linkss = ProtectedLinks.get(key);

            for (List<Link> list : linkss) {
                for (Link link : list) {
                    if (link.getLinkId().getValue().equals(changedLink.getLinkId().getValue())) {
                        if (!affectedIntents.contains(key))
                            affectedIntents.add(key);
                    }
                }
            }
        }
        return affectedIntents;
    }

    /**
     * @param currentLinks
     *            Current links in topology
     * @param newLinks
     *            New links in topology
     * @return Affected links by some change
     */
    public List<Link> identifyChangedLink(List<Link> currentLinks, List<Link> newLinks) {
        List<Link> linksRemoved = new ArrayList<>();

        // Identify if a link is affected in some protected Intent
        for (final Link link : currentLinks) {
            int value = 0;
            for (final Link linkk : newLinks) {
                if (link.getLinkId().equals(linkk.getLinkId())) {
                    value++;
                    break;
                }
            }
            if (value == 0) {
                // Topology hasChanged
                linksRemoved.add(link);
            }
        }
        return linksRemoved;
    }

    /**
     * Returns a list of Network-Topology Links
     * that represents the Suurbale's algorithm
     * for finding shortest pairs of disjoint paths
     * OF Node.
     * @param startVertex Network-Topology Node
     * @param endVertex Network-Topology Node
     * @return Shortest pairs of disjoint paths
     */
    @Override
    public List<List<Link>> getDisjointPaths(NodeId startVertex, NodeId endVertex) {
        Transformer<Link, Double> customTransformer = new Transformer<Link, Double>() {

            @Override
            public Double transform(Link arg0) {
                return new Double(1);
            }
        };
        SuurballeTarjanAlgorithm<NodeId, Link> suurballe = new SuurballeTarjanAlgorithm<>(
                getGraph(), customTransformer,
                true);
        return suurballe.getDisjointPaths(startVertex, endVertex);
    }

    @Override
    public void updateLinks(List<Link> newLink) {
        List<Link> changedLinks = identifyChangedLink(CurrentLinks, newLink);
        List<Intent> affectedIntents = new ArrayList<>();

        for (final Link changedLink : changedLinks) {
            List<Intent> value = getAffectedIntents(changedLink);
            if (!affectedIntents.contains(value)) {
                affectedIntents.addAll(value);
            }
        }

        // Get 2nd route for affected Intents
        for (final Intent intent : affectedIntents) {
            if (ProtectedLinks.containsKey(intent)) {
                List<List<Link>> paths = new ArrayList<>();
                paths.addAll(ProtectedLinks.get(intent));

                // Identify removed paths that contain the links down
                List<Link> removedPath = new ArrayList<>();
                for (final Link changedLink : changedLinks) {
                    final List<Link> removedPathh = identifyRemovedPathByLink(changedLink, paths);

                    if (removedPathh != null && removedPathh.size() > 0) {
                        removedPath.addAll(removedPathh);
                    }

                 // Remove path
                    if (ProtectedLinks.get(intent).contains(removedPathh)) {
                        ProtectedLinks.get(intent).clear();
                    }

                    // Removing the vertex from the network graph
                    networkGraph.removeEdge(changedLink);
                    // networkGraph.removeVertex(changedLink.getDestination().getDestNode());
                }

                // Assign a new path different of the removed one
                List<Link> newPath = getNewPath(paths, removedPath);
                if (newPath != null) {
                    setLinks(newPath);
                    this.changed = true;
                }

                this.message = intent;
                notifyObservers();
            }
        }
    }

    /**
     * @param allPaths All calculated path links
     * @param removedPath List of removed links
     * @return List of other Links
     */
    private List<Link> getNewPath(List<List<Link>> allPaths, List<Link> removedPath) {
        for (final List<Link> link : allPaths) {
            if (link.hashCode() != removedPath.hashCode())
                return link;
        }
        return null;
    }


    /**
     * @param changedLink Updated/Deleted link
     * @param paths All calculated path links
     * @return Path which contains the updated/removed links
     */
    private List<Link> identifyRemovedPathByLink(Link changedLink, List<List<Link>> paths) {
        for (final List<Link> list : paths) {
            for (final Link link : list) {
                if (link.getDestination().getDestNode().getValue()
                        .equals(changedLink.getDestination().getDestNode().getValue())) {
                    return list;
                }
            }
        }
        return null;
    }

    public static void addProtectedLink(
            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent intent,
            List<List<Link>> disjointPaths) {
        ProtectedLinks.put(intent, disjointPaths);
    }

    @Override
    public void register(Observer obj) {
        if (obj == null)
            throw new NullPointerException("Null Observer");
        synchronized (MUTEX) {
            if (!observers.contains(obj)) {
                observers.add(obj);
                obj.setSubject(this);
            }
        }
    }

    @Override
    public void unregister(Observer obj) {
        synchronized (MUTEX) {
            observers.remove(obj);
        }
    }

    @Override
    public void notifyObservers() {
        List<Observer> observersLocal = null;
        // synchronization is used to make sure any observer registered after
        // message is received is not notified
        synchronized (MUTEX) {
            if (!changed)
                return;
            observersLocal = new ArrayList<>(this.observers);
            this.changed = false;
        }
        for (Observer obj : observersLocal) {
            obj.update();
        }
    }

    @Override
    public Object getUpdate(Observer obj) {
        return this.message;
    }
}
