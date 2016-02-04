/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.api;

import org.opendaylight.nic.graph.impl.ClassifierImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.Graph;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.IntentIds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Nodes;

import java.util.Collection;
import java.util.Set;

/**
 * Interface for Compiler/Composed Graph with Input graph as Input produces a composed graph with resolved conflicts
 */
public interface CompilerGraph extends AutoCloseable {


    /**
     * Method to access the input graph, created from the list of intents and return a directed composed graph
     * @param graph the input graph created from the list of updated intents
     * @return      the directed composed graph
     * @throws CompilerGraphException Graph Exception
     */

    // TODO: To be extended to include the whitelist/blacklist composed model
    Collection<InputGraph> compile(Collection<InputGraph> graph)
            throws CompilerGraphException;

    /**
     * Method to access the input graph, created from the list of intents and return a directed composed graph
     * @param csv the source or destination subject
     * @return    Set of Node from the MD-SAL graph
     */
    Set<Nodes> parseEndpointGroup(String csv);

    /**
     * Method to access the input graph, created from the list of intents and return a directed composed graph
     * @param graph the input graph created from the list of updated intents
     * @param flag integer variable to different between compilation of MDSAL graphs and Directed Graphs
     * @return      the directed composed graph
     * @throws CompilerGraphException Graph Exception
     */
    Graph compile(Collection<Graph> graph, int flag) throws CompilerGraphException;

    /** creates an input graph with MD-SAL binding
     *  @param id intent uuid
     *  @param source source node
     *  @param destination destinate node
     *  @param action the Edge attribute with its association to source and destination nodes
     *  @return       the InputGraph from the list of intents
     * */
    InputGraph createGraph(Set<IntentIds> id, Set<Nodes> source, Set<Nodes> destination, Set<Edges> action);

    /** creates an input graph with MD-SAL binding
     *  @param source source node
     *  @param destination destinate node
     *  @param action the Edge attribute with its association to source and destination nodes
     *  @return       the InputGraph from the list of intents
     * */
    InputGraph createGraph(Set<Nodes> source, Set<Nodes> destination, Set<Edges> action);

    /** creates an input graph with MD-SAL binding
     *  @param source source node
     *  @param destination destinate node
     *  @param action the Edge attribute with its association to source and destination nodes
     *  @param classifier intent classifier
     *  @return       the InputGraph from the list of intents
     * */
    InputGraph createGraph(Set<Nodes> source, Set<Nodes> destination, Set<Edges> action, ClassifierImpl classifier);

    /**
     * Method to access the input graph, created from the list of intents and return a directed composed graph
     * @param compiledPolicies the input graph created from the list of updated intents
     * @return      the directed composed graph stored on MDSAL
     */
    Collection<Graph> storeComposedGraph (Collection<InputGraph> compiledPolicies);
}
