/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import com.google.common.collect.Sets;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import org.opendaylight.nic.api.IntentMappingService;
import org.opendaylight.nic.graph.api.CompilerGraph;
import org.opendaylight.nic.graph.api.InputGraph;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Nodes;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CompilerGraphImpl implements CompilerGraph {

    private static final Logger LOG = LoggerFactory
            .getLogger(CompilerGraphImpl.class);

    // Graph representation is utilized for Graph visualization.
    private static DirectedGraph<Set<Nodes>, Set<Edges>> policyGraph =
            new DirectedSparseGraph<Set<Nodes>, Set<Edges>>();

    protected ServiceRegistration<CompilerGraph> graphRegistration;
    protected IntentMappingService intentMappingService;

    public CompilerGraphImpl (IntentMappingService mappingSvc){
        this.intentMappingService = mappingSvc;
    }

    public void addPolicy(InputGraph graph) {

        if (!policyGraph.containsVertex(graph.src())) {
            policyGraph.addVertex(graph.src());
        }
        if (!policyGraph.containsVertex(graph.dst())) {
            policyGraph.addVertex(graph.dst());
        }

        policyGraph
                .addEdge(graph.action(), graph.src(), graph.dst(), EdgeType.DIRECTED);

    }

    public void init() {

        // Initialize operational and default config data in MD-SAL data store
        BundleContext context =
                FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        graphRegistration =
                context.registerService(CompilerGraph.class, this, null);

        LOG.info("Initialization done");
    }

    public void close() throws Exception {
        // Close active registrations
        if (graphRegistration != null) {
            graphRegistration.unregister();
            LOG.info("IntentengineImpl: registrations closed");
        }
    }

    /* Temporary compilation method */
    // TODO: Replaced by Composed graph implementation
    @Override
    public DirectedGraph<Set<Nodes>, Set<Edges>> compile(Collection<InputGraph> policies) {

        DirectedGraph<Set<Nodes>, Set<Edges>> policyGraph1 =
                new DirectedSparseGraph<Set<Nodes>, Set<Edges>>();
        // to be utilization for modifications on the graph
        policyGraph = policyGraph1;

        Queue<InputGraph> conflictingPolicies = new LinkedList<>(policies);
        while (!conflictingPolicies.isEmpty()) {
            InputGraph policy = conflictingPolicies.remove();
            Iterator<InputGraph> iterator2 = conflictingPolicies.iterator();
            Collection<InputGraph> results = new LinkedList<>();
            while (iterator2.hasNext()) {
                InputGraph policy2 = iterator2.next();
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

    private boolean conflicts(InputGraph p1, InputGraph p2) {

        if (p1.src().equals(p2.src()) && p1.dst().equals(p2.dst())) {
            return true;
        }
        return false;
    }

    private Collection<InputGraph> resolve(InputGraph p1, InputGraph p2) {
        Collection<InputGraph> policies = new LinkedList<>();
        policies.add(new InputGraphImpl(p1.src(), p1.dst(), merge(p1.action(),
                p2.action())));
        return policies;

    }
    /**
     * Utilizes the input graph actions or edges types, which can be categorized as different sets that be
     * composed to create a single edge of allow (white listing).
     * @param a1 set of edges or intents
     * @param a2 set of edges or intents
     * @return a composed list of actions
     */
    private Set<Edges> merge(Set<Edges> a1, Set<Edges> a2) {
        Set<Edges> composebleActions = new LinkedHashSet<>();
        Set<Edges> observableActions = new LinkedHashSet<>();
        Set<Edges> exclusiveActions = new LinkedHashSet<>();
        for (Edges action : Sets.union(a1, a2)) {
            switch (action.getType()) {
                case MustAllow:
                    composebleActions.add(action);
                    break;
                case Conditional:
                    composebleActions.add(action);
                    break;
                case CanAllow:
                    observableActions.add(action);
                    break;
                case MustDeny:
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
    public InputGraph createGraph(Set<Nodes> source, Set<Nodes> destination, Set<Edges> action) {
        return new InputGraphImpl (source, destination, action);
    }
}
