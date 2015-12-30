/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
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

public class GraphUtils {
    public static class SuurballeTarjanAlgorithm<V, E> {
        private final Graph<V, E> graph;
        private final Transformer<E, Double> nev;
        private final DijkstraShortestPath<V, E> dijkstra;
        private final boolean cached;

        /**
         * Default constructor. Previous results from the shortest-path
         * algorithm are cached.
         *
         * @param graph
         *            Graph on which shortest paths are searched
         * @param nev
         *            The class responsible for returning weights for edges
         * @since 0.3.0
         */
        public SuurballeTarjanAlgorithm(Graph<V, E> graph, Transformer<E, Double> nev) {
            this(graph, nev, true);
        }

        /**
         * This constructor allows to configure if the shortest-path algorithm
         * should cached previous computations.
         *
         * @param graph
         *            Graph on which shortest paths are searched
         * @param nev
         *            The class responsible for returning weights for edges
         * @param cached
         *            Indicates whether previous computations from the
         *            shortest-path algorithm should be cached
         * @since 0.3.0
         */
        public SuurballeTarjanAlgorithm(Graph<V, E> graph, Transformer<E, Double> nev, boolean cached) {
            this.graph = graph;
            this.nev = nev;
            this.cached = cached;

            dijkstra = new DijkstraShortestPath<V, E>(graph, nev, cached);
        }

        /**
         * <p>
         * Returns the shortest link-disjoint path pair (in increasing order of
         * weight).
         * </p>
         * <p>
         * <b>Important</b>: If only one path can be found, only such a path
         * will be returned.
         * </p>
         *
         * @param startVertex
         *            Start vertex of the calculated paths
         * @param endVertex
         *            Target vertex of the calculated paths
         * @return List of paths in increasing order of weight
         * @since 0.3.0
         */
        public List<List<E>> getDisjointPaths(V startVertex, V endVertex) {
            List<List<E>> linkDisjointSPs = new LinkedList<List<E>>();

            if (!graph.containsVertex(startVertex) || !graph.containsVertex(endVertex) || startVertex.equals(endVertex))
                return linkDisjointSPs;

            /* If target is not reachable, return */
            if (dijkstra.getDistance(startVertex, endVertex) == null)
                return linkDisjointSPs;

            List<E> sp = dijkstra.getPath(startVertex, endVertex);

            /*
             * Determine length of shortest path from "source" to any other node
             */
            Map<V, Number> lengthMap = dijkstra.getDistanceMap(startVertex);

            /* Length transformation */
            Transformer<E, Double> lengthTrans = lengthTransformation(graph, MapTransformer.getInstance(lengthMap));

            /* Get shortest path in g with reversed shortest path... */
            Graph<V, E> revG = reverseEdges(graph, sp);
            DijkstraShortestPath<V, E> revDijkstra = new DijkstraShortestPath<V, E>(revG, lengthTrans, cached);

            Number revDistance = revDijkstra.getDistance(startVertex, endVertex);
            if (revDistance == null || revDistance.doubleValue() == Double.MAX_VALUE) {
                /* no alternate path, return */
                linkDisjointSPs.add(sp);
                return linkDisjointSPs;
            }

            List<E> revSp = revDijkstra.getPath(startVertex, endVertex);

            validatePath(graph, startVertex, endVertex, sp);
            validatePath(revG, startVertex, endVertex, revSp);

            List<E> spCopy = new LinkedList<E>(sp);
            List<List<E>> paths = findDisjointPaths(sp, revSp);

            if (paths == null) {
                /* no disjoint solution found, just return shortest path */
                linkDisjointSPs.add(spCopy);
                return linkDisjointSPs;
            }

            /* Check path validity */
            for (List<E> path : paths)
                validatePath(graph, startVertex, endVertex, path);

            return paths;
        }

        private static <V, E> void validatePath(Graph<V, E> graph, V source, V target, List<E> path) {
            if (!graph.isSource(source, path.get(0)))
                throw new RuntimeException("Bad - Source node is not the first node in the path");

            Iterator<E> it = path.iterator();
            E originVertex = it.next();

            while (it.hasNext()) {
                E destinationVertex = it.next();
                if (!graph.isSource(graph.getDest(originVertex), destinationVertex))
                    throw new RuntimeException("Bad - Path is not contiguous");

                originVertex = destinationVertex;
            }

            if (!graph.isDest(target, path.get(path.size() - 1)))
                throw new RuntimeException("Bad - ");
        }

        /**
         * Combines two disjoint paths from two SuurballeTarjan input paths.
         *
         * @param path1
         *            Dijkstra shortest path
         * @param path2
         *            Dijkstra shortest path in partly reverted graph
         * @return the two disjoint paths
         * @since 0.3.0
         */
        private List<List<E>> findDisjointPaths(List<E> path1, List<E> path2) {
            final V source = graph.getSource(path1.get(0));
            final V target = graph.getDest(path1.get(path1.size() - 1));

            /* First, remove common links */
            Iterator<E> path1_it = path1.iterator();
            while (path1_it.hasNext()) {
                E e1 = path1_it.next();

                Iterator<E> path2_it = path2.iterator();
                while (path2_it.hasNext()) {
                    E e2 = path2_it.next();

                    if (e1.equals(e2)) {
                        if (graph.isSource(source, e1) || graph.isSource(source, e2) || graph.isDest(target, e1)
                                || graph.isDest(target, e2))
                            return null;

                        path1_it.remove();
                        path2_it.remove();
                        break;
                    }
                }
            }

            /* no disjoint solution found */
            if (path1.isEmpty() || path2.isEmpty())
                return null;

            /* Now recombine the two paths */
            List<E> union = ListUtils.union(path1, path2); /* concatenate */

            List<E> p1 = recombinePaths(path1, target, union);
            if (p1 == null)
                return null;

            List<E> p2 = recombinePaths(path2, target, union);
            if (p2 == null)
                return null;

            // if (!union.isEmpty()) throw new RuntimeException("Bad"); /* ToDo:
            // It is an error? */

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

        private List<E> recombinePaths(List<E> path, V target, List<E> union) {
            LinkedList<E> p = new LinkedList<E>(); /* provides getLast */
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
         * This method reverse the path "path" in the graph "graph" and returns
         * it.
         *
         * @param graph
         *            the input graph which will not be changed.
         * @param path
         *            the path to reverse
         * @return a new graph with the reversed path
         * @since 0.3.0
         */
        private static <V, E> Graph<V, E> reverseEdges(Graph<V, E> graph, List<E> path) {
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
         * @param graph1
         *            the graph
         * @param slTrans
         *            The shortest length transformer
         * @return the transformed graph
         * @since 0.3.0
         */
        private Transformer<E, Double> lengthTransformation(Graph<V, E> graph1, Transformer<V, Number> slTrans) {
            Map<E, Double> map = new LinkedHashMap<E, Double>();

            for (E link : graph1.getEdges()) {
                double newWeight;

                if (slTrans.transform(graph1.getSource(link)) == null) {
                    newWeight = Double.MAX_VALUE;
                } else {
                    newWeight = nev.transform(link) - slTrans.transform(graph1.getDest(link)).doubleValue()
                            + slTrans.transform(graph1.getSource(link)).doubleValue();
                    if (Math.abs(newWeight) < 1e-6)
                        newWeight = 0; /* Numerical errors */
                }

                map.put(link, newWeight);
            }

            return MapTransformer.getInstance(map);
        }
    }
}
