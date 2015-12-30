/*
 * Copyright (c) 2016 Inocybe inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.utils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.ListUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.MapTransformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

public final class SuurballeTarjanAlgorithm<V, E> {
    private static final double MIN_WEIGHT = 1e-6;
    private final Graph<V, E> graph;
    private final Transformer<E, Double> nev;
    private final DijkstraShortestPath<V, E> dijkstra;
    private final boolean cached;

    /**
     * Default constructor. Previous results from the shortest-path algorithm
     * are cached.
     *
     * @param graph Graph on which shortest paths are searched
     * @param nev The class responsible for returning weights for edges
     */
    public SuurballeTarjanAlgorithm(final Graph<V, E> graph, final Transformer<E, Double> nev) {
        this(graph, nev, true);
    }

    /**
     * This constructor allows to configure if the shortest-path algorithm
     * should cached previous computations.
     *
     * @param graph Graph on which shortest paths are searched
     * @param nev The class responsible for returning weights for edges
     * @param cached Indicates whether previous computations from the shortest-path
     *            algorithm should be cached
     */
    public SuurballeTarjanAlgorithm(final Graph<V, E> graph, final Transformer<E, Double> nev, final boolean cached) {
        this.graph = graph;
        this.nev = nev;
        this.cached = cached;

        dijkstra = new DijkstraShortestPath<V, E>(graph, nev, cached);
    }

    /**
     * Returns the shortest link-disjoint path pair (in increasing order of
     * weight).
     *
     * @param startVertex Start vertex of the calculated paths
     * @param endVertex Target vertex of the calculated paths
     * @return List of paths in increasing order of weight
     */
    public List<List<E>> getDisjointPaths(final V startVertex, final V endVertex) {
        List<List<E>> linkDisjointSPs = new LinkedList<List<E>>();

        if (!graph.containsVertex(startVertex) || !graph.containsVertex(endVertex) || startVertex.equals(endVertex))
            return linkDisjointSPs;

        // Get distance between start and end vertex
        // If target is not reachable, return
        Number distanceBetweenVertex = dijkstra.getDistance(startVertex, endVertex);
        if (distanceBetweenVertex == null)
            return linkDisjointSPs;

        // Calculate shortest path
        List<E> sp = dijkstra.getPath(startVertex, endVertex);

        // Determine length of shortest path from "source" to any other node
        Map<V, Number> lengthMap = dijkstra.getDistanceMap(startVertex);

        // Length transformation function
        // Modify the cost of each edge in the graph
        Transformer<E, Double> lengthTrans = transformationFunction(graph, MapTransformer.getInstance(lengthMap));

        // Reverse shortest path to get shortest path in g
        Graph<V, E> revG = reverseEdges(graph, sp);
        DijkstraShortestPath<V, E> revDijkstra = new DijkstraShortestPath<V, E>(revG, lengthTrans, cached);

        Number revDistance = revDijkstra.getDistance(startVertex, endVertex);
        if (revDistance == null || revDistance.doubleValue() == Double.MAX_VALUE) {
            // No disjoint path
            linkDisjointSPs.add(sp);
            return linkDisjointSPs;
        }

        List<E> revSp = revDijkstra.getPath(startVertex, endVertex);

        verifyPath(graph, startVertex, endVertex, sp);
        verifyPath(revG, startVertex, endVertex, revSp);

        List<E> spCopy = new LinkedList<E>(sp);
        List<List<E>> paths = getDisjointPaths(sp, revSp);

        if (paths == null) {
            // No disjoint path found, return shortest path
            linkDisjointSPs.add(spCopy);
            return linkDisjointSPs;
        }

        // Verify paths
        for (List<E> path : paths)
            verifyPath(graph, startVertex, endVertex, path);

        return paths;
    }

    /**
     * This method verifies graph paths
     * @param graph Graph
     * @param source Source vertex
     * @param target Destination vertex
     * @param path Path
     */
    private static <V, E> void verifyPath(final Graph<V, E> graph, final V source, final V target, final List<E> path) {
        if (!graph.isSource(source, path.get(0)))
            throw new RuntimeException("Source node is not the first node in the path");

        E originVertex = path.get(0);

        for (E destinationVertex : path.subList(1, path.size() - 1)) {
            if (!graph.isSource(graph.getDest(originVertex), destinationVertex))
                throw new RuntimeException("Path is not contiguous");

            originVertex = destinationVertex;
        }

        if (!graph.isDest(target, path.get(path.size() - 1)))
            throw new RuntimeException("Isn't destination ");
    }

    /**
     * Concatenates two disjoint paths from two SuurballeTarjan input paths.
     *
     * @param path1 Dijkstra shortest path
     * @param path2 Dijkstra shortest path in partly reverted graph
     * @return the two disjoint paths
     */
    private List<List<E>> getDisjointPaths(final List<E> path1, final List<E> path2) {
        final V source = graph.getSource(path1.get(0));
        final V target = graph.getDest(path1.get(path1.size() - 1));

        // Remove common links
        Iterator<E> path1_iterator = path1.iterator();
        while (path1_iterator.hasNext()) {
            E e1 = path1_iterator.next();

            Iterator<E> path2_iterator = path2.iterator();
            while (path2_iterator.hasNext()) {
                E e2 = path2_iterator.next();

                if (e1.equals(e2)) {
                    if (graph.isSource(source, e1) || graph.isSource(source, e2) || graph.isDest(target, e1)
                            || graph.isDest(target, e2))
                        return null;

                    path1_iterator.remove();
                    path2_iterator.remove();
                    break;
                }
            }
        }

        // No disjoint path found
        if (path1.isEmpty() || path2.isEmpty())
            return null;

        // Concatenate the two paths */
        List<E> union = ListUtils.union(path1, path2);

        List<E> p1 = mergePaths(path1, target, union);
        if (p1 == null)
            return null;

        List<E> p2 = mergePaths(path2, target, union);
        if (p2 == null)
            return null;

        List<List<E>> solution = new LinkedList<List<E>>();

        double path1_cost = 0;
        for (E edge : p1)
            path1_cost += nev.transform(edge);

        double path2_cost = 0;
        for (E edge : p2)
            path2_cost += nev.transform(edge);

        if (path1_cost <= path2_cost) {
            solution.add(p1);
            solution.add(p2);
        } else {
            solution.add(p2);
            solution.add(p1);
        }

        return solution;
    }

    /** This method merges paths with common vertex
     * @param path Path
     * @param target Target vertex
     * @param union United paths
     * @return Merged paths
     */
    private List<E> mergePaths(List<E> path, V target, List<E> union) {
        LinkedList<E> p = new LinkedList<E>();
        p.add(path.get(0));
        union.remove(path.get(0));

        V curDest;
        while (!(curDest = graph.getDest(p.getLast())).equals(target)) {
            boolean progress = false;
            for (E e : union) {
                if (graph.isSource(curDest, e)) {
                    p.add(e);
                    progress = true;
                    union.remove(e);
                    break;
                }
            }

            if (!progress)
                return null;

            if (union.isEmpty()) {
                if (!graph.isDest(target, p.getLast()))
                    throw new RuntimeException("Bad");
                else
                    break;
            }
        }
        return p;
    }

    /**
     * This method reverse the path "path" in the graph "graph" and returns it.
     *
     * @param graph the input graph which will not be changed.
     * @param path the path to reverse
     * @return a new graph with the reversed path
     */
    private static <V, E> Graph<V, E> reverseEdges(final Graph<V, E> graph, final List<E> path) {
        if (graph == null || path == null)
            throw new IllegalArgumentException();
        Graph<V, E> clone = new DirectedOrderedSparseMultigraph<V, E>();

        for (V v : graph.getVertices())
            clone.addVertex(v);
        for (E e : graph.getEdges())
            clone.addEdge(e, graph.getEndpoints(e));

        for (E link : path) {
            V src = clone.getSource(link);
            V dst = clone.getDest(link);
            clone.removeEdge(link);
            clone.addEdge(link, dst, src, EdgeType.DIRECTED);
        }

        return clone;
    }

    /**
     * This method does the following length transformation:
     *
     * <pre>
     *  c'(v,w) = c(v,w) - d (s,w) + d (s,v)
     * </pre>
     *
     * @param graph1 the graph
     * @param slTrans The shortest length transformer
     * @return the transformed graph
     */
    private Transformer<E, Double> transformationFunction(final Graph<V, E> graph1, final Transformer<V, Number> slTrans) {
        Map<E, Double> map = new LinkedHashMap<E, Double>();

        for (E link : graph1.getEdges()) {
            double newWeight;

            if (slTrans.transform(graph1.getSource(link)) == null) {
                newWeight = Double.MAX_VALUE;
            } else {
                newWeight = nev.transform(link) - slTrans.transform(graph1.getDest(link)).doubleValue()
                        + slTrans.transform(graph1.getSource(link)).doubleValue();
                if (Math.abs(newWeight) < MIN_WEIGHT)
                    newWeight = 0;
            }

            map.put(link, newWeight);
        }

        return MapTransformer.getInstance(map);
    }
}