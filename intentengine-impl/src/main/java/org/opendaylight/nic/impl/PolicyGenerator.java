//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.opendaylight.nic.compiler.Edge;
import org.opendaylight.nic.compiler.Epg;
import org.opendaylight.nic.compiler.api.Endpoint;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;

public class PolicyGenerator {

    private DirectedGraph<Epg, Edge> gr = new DirectedSparseGraph<Epg, Edge>();
    private Map<Epg, Set<Endpoint>> map = new HashMap<Epg, Set<Endpoint>>();

    public PolicyGenerator(DirectedGraph<Epg, Edge> gr,
            Map<Epg, Set<Endpoint>> map) {
        this.gr = gr;
        this.map = map;

    }

    public Collection<Policy> generate() {
        Collection<Policy> generatedPolicy = new LinkedList<Policy>();

        Collection<Edge> edges = gr.getEdges();
        for (Edge edge : edges) {

            Pair<Epg> epgPair = gr.getEndpoints(edge);

            generatedPolicy.add(new PolicyImpl(map.get(epgPair.getFirst()), map
                    .get(epgPair.getSecond()), edge.getAction()));

        }

        return generatedPolicy;

    }
}