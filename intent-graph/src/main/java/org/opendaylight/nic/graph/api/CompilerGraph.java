/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Nodes;
import edu.uci.ics.jung.graph.DirectedGraph;
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
    DirectedGraph<Set<Nodes>, Set<Edges>> compile(Collection<InputGraph> graph)
            throws CompilerGraphException;

    /** creates an input graph with MD-SAL binding
     *  @param source source node
     *  @param destination destinate node
     *  @param action the Edge attribute with its association to source and destination nodes
     *  @return       the InputGraph from the list of intents
     * */
    InputGraph createGraph(Set<Nodes> source, Set<Nodes> destination, Set<Edges> action);
}
