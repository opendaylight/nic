/*
 * Copyright © 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.algorithm;

import org.apache.commons.collections15.ListUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.MapTransformer;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SuurballeAlgorithm<V, E> {
    private final DijkstraShortestPath<V, E> dijkstraAlgorithm;
    private final Graph<V, E> graph;
    private final Transformer<E, Number> defaultEdgeWeight = arg0 -> (Number)new Double(1);
    Map<V, Number> initialCostMap = null;

    public SuurballeAlgorithm(Graph<V,E> graph) {
        this.graph = graph;
        this.dijkstraAlgorithm = new DijkstraShortestPath<>(graph, defaultEdgeWeight, true);
    }

    public final List<List<E>> findShortestPath(final V initial, final V destination){
        final List<E> shortestPath = dijkstraAlgorithm.getPath(initial, destination);
        initialCostMap = dijkstraAlgorithm.getDistanceMap(initial);

        final List<E> reversedShortestPath =
                reverseUpdateEdgesWeight(graph, MapTransformer.getInstance(initialCostMap),
                        shortestPath, initial, destination);

        discardCommonReversedEdges(graph, shortestPath, reversedShortestPath);

        final List<E> unitedPaths = ListUtils.union(shortestPath, reversedShortestPath);
        final List<E> resultPath1 = restorePaths(shortestPath, destination, unitedPaths);
        final List<E> resultPath2 = restorePaths(reversedShortestPath, destination, unitedPaths);
        List<List<E>> result = mergePaths(resultPath1, resultPath2);

        if ((result == null) || (result.size() == 0)){
            result = new ArrayList<>();
            result.add(shortestPath);
        }
        return result;
    }

    private List<List<E>> mergePaths(final List<E> resultPath1, final List<E> resultPath2) {
        final List<List<E>> result = new ArrayList<>();
        final Number cost1 = calculatePathCost(resultPath1);
        final Number cost2 = calculatePathCost(resultPath2);

        if (cost1.doubleValue() > cost2.doubleValue()){
            result.add(resultPath1);
            result.add(resultPath2);
        } else {
            result.add(resultPath2);
            result.add(resultPath1);
        }
        return result;
    }

    private void discardCommonReversedEdges(final Graph<V,E> graph, final List<E> path1, final List<E> path2) {
        if (path1.size() == 0 || path2.size() == 0){
            return;
        } else {
            final V source = graph.getSource(path1.get(0));
            final V target = graph.getDest(path1.get(path1.size() - 1));

            for(final E edge2 : path2){
                for(final E edge1 : path1){
                    if (edge1.equals(edge2)){
                        if (graph.isSource(source, edge1) ||
                                graph.isSource(source, edge2) ||
                                graph.isDest(target, edge1) ||
                                graph.isDest(target, edge2)){
                            // Return only shortest path
                            path2.clear();
                            return;
                        }
                        path1.remove(edge1);
                        path2.remove(edge2);
                        break;
                    }
                }
            }
        }
    }

    protected List<E> reverseUpdateEdgesWeight(final Graph<V, E> graph, final  Transformer<V, Number> transformer,
                                               final List<E> shortestPath, final V initial, final V destination) {
        for(final E edge1 : shortestPath){
            V src = graph.getSource(edge1);
            V dst = graph.getDest(edge1);
            graph.removeEdge(edge1);
            graph.addEdge(edge1, dst, src, EdgeType.DIRECTED);
        }

        final List<E> edges = new ArrayList<>(graph.getEdges());
        final Map<E, Number> map = new LinkedHashMap<>();
        edges.forEach(edge -> {
            final V source = graph.getSource(edge);
            final V dest = graph.getDest(edge);
            Number cost = calculateCost(transformer, edge, source, dest);
            map.put(edge,cost);
        });

        final DijkstraShortestPath<V, E> reversedDijkstra =
                new DijkstraShortestPath<>(graph, MapTransformer.getInstance(map));

        DijkstraShortestPath<V, E> validatedShortestPath = checkPath(initial, destination, reversedDijkstra);
        return validatedShortestPath != null ? reversedDijkstra.getPath(initial, destination) : new ArrayList<>();
    }

    private DijkstraShortestPath<V, E> checkPath(final V startPoint, final V endPoint,
                                                 final DijkstraShortestPath<V, E> reversedDijkstra) {
        final Number reversedDistance = reversedDijkstra.getDistance(startPoint, endPoint);
        if ((reversedDistance == null) || (reversedDistance.doubleValue() == Double.MAX_VALUE)){
            return null;
        }
        return reversedDijkstra;
    }

    private Number calculateCost(final Transformer<V, Number> transformer, final E edge,
                                 final V source, final V destination) {
        double cost = 0;
        if (transformer.transform(source) != null) {
            double edgeWeight = defaultEdgeWeight.transform(edge).doubleValue();
            double destinationWeight = (transformer.transform(destination).doubleValue());
            double sourceWeight = transformer.transform(source).doubleValue();
            cost = (edgeWeight - (destinationWeight + sourceWeight));
            if (Math.abs(cost) < 0.000001) {
                cost = 0;
            } else {
                cost = Double.POSITIVE_INFINITY;
            }
        }
        return cost;
    }

    private List<E> restorePaths(final List<E> path, final V target, final List<E> partialMergedPaths) {
        final List<E> resultPath = new ArrayList<>();
        resultPath.add(path.get(0));
        partialMergedPaths.remove(path.get(0));

        if (!isDestination(target, resultPath)){
            V currentDestination = graph.getDest(resultPath.get(resultPath.size() - 1));
            for (E edge : partialMergedPaths){
                if (graph.isSource(currentDestination, edge)) {
                    resultPath.add(edge);
                    partialMergedPaths.remove(edge);
                    break;
                }
            }
        }
        return resultPath;
    }

    private boolean isDestination(final V target, final List<E> resultPath) {
        return graph.getDest(resultPath.get(resultPath.size() - 1)).equals(target);
    }

    private double calculatePathCost(final List<E> path){
        double result = 0;
        for(E edge : path) {
            result += defaultEdgeWeight.transform(edge).doubleValue();
        }
        return result;
    }
}

