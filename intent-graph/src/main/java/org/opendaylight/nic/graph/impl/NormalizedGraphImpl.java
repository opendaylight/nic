/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import com.google.common.collect.Sets;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Nodes;

import java.util.Set;

public class NormalizedGraphImpl {
    /*
     * Using AND and OR functions, create normalized EPG SRC & DST.
     * Normalized class will have a srcN = (CMP & MKT) and dstN = (web & DB)
     */
    protected IntentMappingService labelRelationMap;

    /* Method to normalize source and destination nodes
     * Normalization is done only if conditions match, else the list remains
     * For example: Src node 1 and Src node 2 has a relationship, then new src = (src node 1 & src node 2)
     */
    public Set<InputGraphImpl> normalizeNode (Set<InputGraphImpl> graphs) {

        RelationMapImpl relationMap = new RelationMapImpl(labelRelationMap);
        Set<Nodes> srcN = null;
        Set<Nodes> dstN = null;
        Set<Edges> actionN = null;

        for (InputGraphImpl graph1 : graphs ) {
            for (InputGraphImpl graph2 : graphs ) {
                if (relationMap.hasRelation(graph1.src().toString(), graph2.src().toString())
                        && graph1.action().contains(graph2.action())) {
                    srcN = Sets.union(graph1.src(), graph2.src());
                }
                if (relationMap.hasRelation(graph1.dst().toString(), graph2.dst().toString())
                        && graph1.action().contains(graph2.action())) {
                    dstN = Sets.union(graph1.dst(), graph2.dst());
                }
                if (graph1.action().contains(graph2.action()) && graph2.action().contains(graph1.action())) {
                    actionN = Sets.union(graph1.action(), graph2.action());
                }
                InputGraphImpl graphN = new InputGraphImpl(srcN, dstN, actionN);
                if (graphN.src != null && graphN.dst != null && graphN.action != null) {
                    graphs.remove(graph1);
                    graphs.remove(graph2);
                    graphs.add(graphN);
                }
            }
        }

        return graphs;
    }
}
