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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.opendaylight.nic.compiler.Edge;
import org.opendaylight.nic.compiler.Epg;
import org.opendaylight.nic.compiler.api.Action;
import org.opendaylight.nic.compiler.api.Endpoint;

import com.google.common.collect.Sets;

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

        Collection<Policy> finalPolicy = new LinkedList<Policy>();
        finalPolicy = removeOverlap(generatedPolicy);

        return finalPolicy;

    }

    public Collection<Policy> removeOverlap(Collection<Policy> policies) {
        Queue<Policy> conflictingPolicies = new LinkedList<>(policies);
        Collection<Policy> compiledPolicies = new LinkedList<>();
        while (!conflictingPolicies.isEmpty()) {
            Policy policy = conflictingPolicies.remove();
            Iterator<Policy> iterator2 = conflictingPolicies.iterator();
            Collection<Policy> results = new LinkedList<>();
            while (iterator2.hasNext()) {
                Policy policy2 = iterator2.next();
                if (conflicts(policy, policy2)) {
                    iterator2.remove();
                    results.addAll(transform(policy, policy2));
                }
            }
            if (results.isEmpty()) {
                compiledPolicies.add(policy);
            } else {
                conflictingPolicies.addAll(results);
            }
        }
        return compiledPolicies;
    }

    private Collection<Policy> transform(Policy p1, Policy p2) {
        Collection<Policy> policies = new LinkedList<>();
        Sets.SetView<Endpoint> src, dst;

        src = Sets.difference(p1.src(), p2.src());
        if (!src.isEmpty()) {
            dst = Sets.difference(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, p1.action()));
            }
            dst = Sets.intersection(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, p1.action()));
            }
        }
        src = Sets.intersection(p1.src(), p2.src());
        if (!src.isEmpty()) {
            dst = Sets.intersection(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, merge(p1.action(),
                        p2.action())));
            }
            dst = Sets.difference(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, p1.action()));
            }
            dst = Sets.difference(p2.dst(), p1.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, p2.action()));
            }
        }
        src = Sets.difference(p2.src(), p1.src());
        if (!src.isEmpty()) {
            dst = Sets.intersection(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, p2.action()));
            }
            dst = Sets.difference(p2.dst(), p1.dst());
            if (!dst.isEmpty()) {
                policies.add(new PolicyImpl(src, dst, p2.action()));
            }
        }

        return policies;
    }

    private Set<Action> merge(Set<Action> a1, Set<Action> a2) {
        Set<Action> composebleActions = new LinkedHashSet<>();
        Set<Action> observableActions = new LinkedHashSet<>();
        Set<Action> exclusiveActions = new LinkedHashSet<>();
        for (Action action : Sets.union(a1, a2)) {
            switch (action.getType()) {
                case COMPOSABLE:
                    composebleActions.add(action);
                    break;
                case OBSERVER:
                    observableActions.add(action);
                    break;
                case EXCLUSIVE:
                    exclusiveActions.add(action);
                    break;
                default:
                    return null;
            }
        }
        if (!exclusiveActions.isEmpty()) {
            if (exclusiveActions.size() == 1) {
                return Sets.union(exclusiveActions, observableActions);
            } else {
                return null;
            }
        }
        return Sets.union(composebleActions, observableActions);
    }

    private boolean conflicts(Policy p1, Policy p2) {
        if (!Sets.intersection(p1.src(), p2.src()).isEmpty()
                && !Sets.intersection(p1.dst(), p2.dst()).isEmpty())
            return true;
        return false;
    }
}
