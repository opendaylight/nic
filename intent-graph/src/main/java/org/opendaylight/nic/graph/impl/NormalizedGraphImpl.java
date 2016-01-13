/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import com.google.common.collect.Sets;
import org.opendaylight.nic.graph.api.InputGraph;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Nodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class NormalizedGraphImpl {
    private static final Logger LOG = LoggerFactory
            .getLogger(NormalizedGraphImpl.class);
    /*
     * Using AND and OR functions, create normalized EPG SRC & DST.
     * Normalized class will have a srcN = (CMP & MKT) and dstN = (web & DB)
     */
    protected IntentMappingService labelRelationMap;

    public NormalizedGraphImpl (IntentMappingService mappingSvc){
        this.labelRelationMap = mappingSvc;
    }

    /* Method to normalize source and destination nodes
     * Normalization is done only if conditions match, else the list remains
     * For example: Src node 1 and Src node 2 has a relationship, then new src = (src node 1 & src node 2)
     */
    public Collection<InputGraph> normalizedGraph (Collection<InputGraph> graphs) {

        RelationMapImpl relationMap = new RelationMapImpl(labelRelationMap);
        Set<Nodes> srcN = new HashSet<>();
        Set<Nodes> dstN = new HashSet<>();
        Set<Edges> actionN = new HashSet<>();
        Queue<InputGraph> graphsTemp = new LinkedList<>(graphs);
        Collection<InputGraph> graphsN = new LinkedList<>();

        while (!graphsTemp.isEmpty()) {
            InputGraph graph1 = graphsTemp.remove();
            for (InputGraph graph2 : graphsTemp) {
                if (relationMap.hasRelation(graph1.src().toString(), graph2.src().toString())
                        && graph1.action().equals(graph2.action())) {
                    srcN = Sets.union(graph1.src(), graph2.src());
                }
                if (relationMap.hasRelation(graph1.dst().toString(), graph2.dst().toString())
                        && graph1.action().equals(graph2.action())) {
                    dstN = Sets.union(graph1.dst(), graph2.dst());
                }
                if (graph1.action().equals(graph2.action()) && graph2.action().equals(graph1.action())) {
                    actionN = Sets.intersection(graph1.action(), graph2.action());
                }
            }
            InputGraph graphN = new InputGraphImpl(srcN, dstN, actionN);
            if (!graphN.src().isEmpty() && !graphN.dst().isEmpty() && !graphN.action().isEmpty()) {
                graphsN.add(graphN);
            } else {
                graphsN.add(new InputGraphImpl(graph1.src(), graph1.dst(), graph1.action()));
            }
        }
        LOG.info("Normalized.");
        /* @param graphsN will be used for the composed graph */
        return graphsN;
    }
}
