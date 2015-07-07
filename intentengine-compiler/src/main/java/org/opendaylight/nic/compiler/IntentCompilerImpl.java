//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.opendaylight.nic.compiler.api.Action;
import org.opendaylight.nic.compiler.api.IntentCompiler;
import org.opendaylight.nic.compiler.api.Policy;
import org.opendaylight.nic.impl.EndpointChangeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;

import com.google.common.collect.Sets;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

// The class for maintaining the graph of policies : It accepts src EPG, dst EPG and
// associated actions and puts them in the graph.

public class IntentCompilerImpl implements IntentCompiler {

    private static DirectedGraph<Epg, Edge> policyGraph =
            new DirectedSparseGraph<Epg, Edge>();

    public void addPolicy(Policy policy) {

        if (!policyGraph.containsVertex(policy.src())) {
            policyGraph.addVertex(policy.src());
        }
        if (!policyGraph.containsVertex(policy.dst())) {
            policyGraph.addVertex(policy.dst());
        }

        Edge edge = new Edge(policy.action());
        policyGraph
                .addEdge(edge, policy.src(), policy.dst(), EdgeType.DIRECTED);

    }

    @Override
    public DirectedGraph<Epg, Edge> compile(Collection<Policy> policies) {

        // print out nodes
        for (Node key : EndpointChangeListener.nodeMap.keySet()) {

            System.out.println(key.toString() + " HOSTS"
                    + EndpointChangeListener.nodeMap.get(key).toString());

        }

        // / end printing

        Queue<Policy> conflictingPolicies = new LinkedList<>(policies);
        while (!conflictingPolicies.isEmpty()) {
            Policy policy = conflictingPolicies.remove();
            Iterator<Policy> iterator2 = conflictingPolicies.iterator();
            Collection<Policy> results = new LinkedList<>();
            while (iterator2.hasNext()) {
                Policy policy2 = iterator2.next();
                if (conflicts(policy, policy2)) {
                    iterator2.remove();
                    results.addAll(resolve(policy, policy2));
                }
            }
            if (results.isEmpty()) {
                addPolicy(policy);
            } else {
                conflictingPolicies.addAll(results);
            }
        }
        return policyGraph;
    }

    private boolean conflicts(Policy p1, Policy p2) {

        if (p1.src().equals(p2.src()) && p1.dst().equals(p2.dst())) {
            return true;
        }
        return false;
    }

    private Collection<Policy> resolve(Policy p1, Policy p2) {
        Collection<Policy> policies = new LinkedList<>();
        policies.add(new PolicyImpl(p1.src(), p1.dst(), merge(p1.action(),
                p2.action())));
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

    @Override
    public Policy createPolicy(Epg source, Epg destination, Set<Action> action) {
        return new PolicyImpl(source, destination, action);
    }
}