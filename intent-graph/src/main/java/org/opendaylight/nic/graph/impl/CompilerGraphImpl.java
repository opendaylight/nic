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
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import org.opendaylight.nic.graph.api.CompilerGraph;
import org.opendaylight.nic.graph.api.CompilerGraphException;
import org.opendaylight.nic.graph.api.InputGraph;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.Graph;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.GraphBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.NodesBuilder;
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
    private static Graph policyGraphMDSAL = new GraphBuilder().build();

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

        //TODO: store the graph in MD-SAL

    }

    public void addPolicy(Graph graph) {

        if (!policyGraphMDSAL.getNodes().equals(graph.getNodes())) {
            policyGraphMDSAL.getNodes().add((Nodes) graph.getNodes());
        }
        if (!policyGraphMDSAL.getEdges().equals(graph.getEdges())) {
            policyGraphMDSAL.getEdges().add((Edges) graph.getEdges());
        }

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

    @Override
    public Set<Nodes> parseEndpointGroup(String csv) {
        Set<Nodes> endpoints = new LinkedHashSet<>();
        String[] labels = csv.split(",");
        for (String label : labels) {
            endpoints.add(new NodesBuilder().setName(label).build());
        }
        return endpoints;
    }

    /* Temporary compilation method */
    // TODO: Replaced by Composed graph implementation
    @Override
    public DirectedGraph<Set<Nodes>, Set<Edges>> compile(Collection<InputGraph> policies) throws CompilerGraphException {

        DirectedGraph<Set<Nodes>, Set<Edges>> policyGraph1 = new DirectedSparseGraph<Set<Nodes>, Set<Edges>>();
        Graph policyGraph2 = new GraphBuilder().build();
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

    @Override
    public Graph compile(Collection<Graph> policies, int flag) throws CompilerGraphException {

        Graph policyGraph1 = new GraphBuilder().build();
        // to be utilization for modifications on the graph
        policyGraphMDSAL = policyGraph1;

        Queue<Graph> conflictingPolicies = new LinkedList<>(policies);
        while (!conflictingPolicies.isEmpty()) {
            Graph policy = conflictingPolicies.remove();
            Iterator<Graph> iterator2 = conflictingPolicies.iterator();
            Collection<Graph> results = new LinkedList<>();
            while (iterator2.hasNext()) {
                Graph policy2 = iterator2.next();
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
        return policyGraphMDSAL;
    }

    private boolean conflicts(InputGraph p1, InputGraph p2) throws NullPointerException {
        ClassifierImpl ci;

        if ((p1.classifier().equals(ClassifierImpl.getInstance(ExpressionImpl.EXPRESSION_NULL)))
                && (p2.classifier().equals(ClassifierImpl.getInstance(ExpressionImpl.EXPRESSION_NULL)))) {
            if (!Sets.intersection(p1.src(), p2.src()).isEmpty()
                    && !Sets.intersection(p1.dst(), p2.dst()).isEmpty()) {
                return true;
            }
        } else {
            ci = (p1.classifier()).and(p2.classifier());
            if (!Sets.intersection(p1.src(), p2.src()).isEmpty()
                    && !Sets.intersection(p1.dst(), p2.dst()).isEmpty()
                    && !ci.isEmpty()) {
                return true;
            }
        }
        //if (p1.src().equals(p2.src()) && p1.dst().equals(p2.dst())) {
        //    return true;
        //}
        return false;
    }

    private boolean conflicts(Graph p1, Graph p2) throws NullPointerException {
        ClassifierImpl ci;

        if ((p1.getClassifiers().equals(ClassifierImpl.getInstance(ExpressionImpl.EXPRESSION_NULL)))
                && (p2.getClassifiers().equals(ClassifierImpl.getInstance(ExpressionImpl.EXPRESSION_NULL)))) {
            if (!p1.getEdges().contains(p2.getEdges())) {
                return true;
            }
        } else {
            if (!p1.getEdges().contains(p2.getEdges())
                    && !p1.getClassifiers().contains(p2.getClassifiers())) {
                return true;
            }
        }
        //if (p1.src().equals(p2.src()) && p1.dst().equals(p2.dst())) {
        //    return true;
        //}
        return false;
    }

    /* Classifiers are used to resolve the
     * @param p1  */
    private Collection<InputGraph> resolve(InputGraph p1, InputGraph p2) throws CompilerGraphException {

        Collection<InputGraph> policies = new LinkedList<>();
        Sets.SetView<Nodes> src;
        Sets.SetView<Nodes> dst;
        ClassifierImpl ci;
        boolean nc = true;

        // All the possible cases below

        src = Sets.difference(p1.src(), p2.src());
        if (!src.isEmpty()) {

            // Case: S1 and not S2 , D1 and not D2
            dst = Sets.difference(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {

                // C1 and not C2
                ci = (p1.classifier()).sub(p2.classifier());
                if (!ci.isEmpty()) {
                    policies.add(new InputGraphImpl(src, dst, p1.action(), ci));
                    nc = false;
                }
                // C1 and C2
                ci = (p1.classifier()).and(p2.classifier());
                if (!ci.isEmpty()) {
                    policies.add(new InputGraphImpl(src, dst, p1.action(), ci));
                    nc = false;
                }
                if (nc) {
                    policies.add(new InputGraphImpl(src, dst, p1.action()));
                }
            }

            // Case: S1 and not S2 , D1 and D2
            dst = Sets.intersection(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {

                // C1 and not C2
                ci = (p1.classifier()).sub(p2.classifier());
                if (!ci.isEmpty()) {
                    policies.add(new InputGraphImpl(src, dst, p1.action(), ci));
                    nc = false;
                }
                // C1 and C2
                ci = (p1.classifier()).and(p2.classifier());
                if (!ci.isEmpty()) {
                    policies.add(new InputGraphImpl(src, dst, p1.action(), ci));
                    nc = false;
                }
                if (nc) {
                    policies.add(new InputGraphImpl(src, dst, p1.action()));
                }
            }
        }
        src = Sets.intersection(p1.src(), p2.src());
        if (!src.isEmpty()) {

            // Case: S1 and S2 , D1 and D2
            dst = Sets.intersection(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {

                // C1 and not C2
                ci = (p1.classifier()).sub(p2.classifier());
                if (!ci.isEmpty()) {
                    policies.add(new InputGraphImpl(src, dst, p1.action(), ci));
                    nc = false;
                }
                // C1 and C2
                ci = (p1.classifier()).and(p2.classifier());
                if (!ci.isEmpty()) {
                    Set<Edges> mergedActions = merge(p1.action(), p2.action());
                    if (mergedActions == null) {
                        throw new CompilerGraphException(
                                "Unable to merge exclusive actions",
                                Arrays.asList(p1, p2));
                    }

                    policies.add(new InputGraphImpl(src, dst, mergedActions, ((p1
                            .classifier()).and(p2.classifier()))));
                    nc = false;
                }
                // C2 and not C1
                ci = (p2.classifier()).sub(p1.classifier());
                if (!ci.isEmpty()) {
                    policies.add(new InputGraphImpl(src, dst, p2.action(), ci));
                    nc = false;
                }
                if (nc) {
                    Set<Edges> mergedActions = merge(p1.action(), p2.action());
                    if (mergedActions == null) {
                        throw new CompilerGraphException(
                                "Unable to merge exclusive actions",
                                Arrays.asList(p1, p2));
                    }
                    policies.add(new InputGraphImpl(src, dst, mergedActions));
                }
            }

            // Case: S1 and S2 , D1 and not D2
            dst = Sets.difference(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {

                // C1 and not C2
                ci = (p1.classifier()).sub(p2.classifier());
                if (!ci.isEmpty()) {
                    policies.add(new InputGraphImpl(src, dst, p1.action(), ci));
                    nc = false;
                }
                // C1 and C2
                ci = (p1.classifier()).and(p2.classifier());
                if (!ci.isEmpty()) {
                    policies.add(new InputGraphImpl(src, dst, p1.action(), ci));
                    nc = false;
                }
                if (nc) {
                    policies.add(new InputGraphImpl(src, dst, p1.action()));
                }
            }

            // Case: S1 and S2 , D2 and not D1
            dst = Sets.difference(p2.dst(), p1.dst());
            if (!dst.isEmpty()) {

                // C1 and C2
                ci = (p1.classifier()).and(p2.classifier());
                if (!ci.isEmpty()) {
                    policies.add(new InputGraphImpl(src, dst, p2.action(), ci));
                    nc = false;
                }
                // C2 and not C1
                ci = (p2.classifier()).sub(p1.classifier());
                if (!ci.isEmpty()) {
                    policies.add(new InputGraphImpl(src, dst, p2.action(), ci));
                    nc = false;
                }
                if (nc) {
                    policies.add(new InputGraphImpl(src, dst, p2.action()));
                }
            }
        }
        src = Sets.difference(p2.src(), p1.src());
        if (!src.isEmpty()) {

            // Case: S2 and not S1 , D1 and D2
            dst = Sets.intersection(p1.dst(), p2.dst());
            if (!dst.isEmpty()) {

                // C1 and C2
                ci = (p1.classifier()).and(p2.classifier());
                if (!ci.isEmpty()) {
                    policies.add(new InputGraphImpl(src, dst, p2.action(), ci));
                    nc = false;
                }
                // C2 and not C1
                ci = (p2.classifier()).sub(p1.classifier());
                if (!ci.isEmpty()) {
                    policies.add(new InputGraphImpl(src, dst, p2.action(), ci));
                    nc = false;
                }
                if (nc) {
                    policies.add(new InputGraphImpl(src, dst, p2.action()));
                }

            }
            // Case: S2 and not S1 , D2 and not D1
            dst = Sets.difference(p2.dst(), p1.dst());
            if (!dst.isEmpty()) {

                // C2 and C1
                ci = (p1.classifier()).and(p2.classifier());
                if (!ci.isEmpty()) {
                    policies.add(new InputGraphImpl(src, dst, p2.action(), ci));
                    nc = false;
                }
                // C2 and not C1
                ci = (p2.classifier()).sub(p1.classifier());
                if (!ci.isEmpty()) {
                    policies.add(new InputGraphImpl(src, dst, p2.action(), ci));
                    nc = false;
                }
                if (nc) {
                    policies.add(new InputGraphImpl(src, dst, p2.action()));
                }
            }
        }
        //policies.add(new InputGraphImpl(p1.src(), p1.dst(), merge(p1.action(),p2.action()), ci));
        return policies;

    }

    private Collection<Graph> resolve(Graph p1, Graph p2) throws CompilerGraphException {

        Collection<Graph> policies = new LinkedList<>();

        // TODO All the possible cases below
        policies.add(new GraphBuilder().setNodes(p1.getNodes()).setNodes(p2.getNodes()).setEdges(p1.getEdges()).setEdges(p2.getEdges()).build());

        //policies.add(new InputGraphImpl(p1.src(), p1.dst(), merge(p1.action(),p2.action()), ci));
        return policies;

    }

    /**
     * Utilizes the input graph actions or edges types, which can be categorized as different sets that be
     * composed to create a single edge of allow (white listing).
     * @param a1 set of edges or intents
     * @param a2 set of edges or intents
     * @return a composed list of actions
     */
    private Set<Edges> merge(Set<Edges> a1, Set<Edges> a2) throws CompilerGraphException {
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

    @Override
    public InputGraph createGraph(Set<Nodes> source, Set<Nodes> destination, Set<Edges> action, ClassifierImpl classifier) {
        return new InputGraphImpl (source, destination, action, classifier);
    }
}
