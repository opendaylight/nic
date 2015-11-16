/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.input.graph.Edges;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.input.graph.Nodes;
import edu.uci.ics.jung.graph.DirectedGraph;
import java.util.Collection;
import java.util.Set;

public interface CompilerGraph {

    // TODO: To be extended to include the whitelist/blacklist composed model
    DirectedGraph<Nodes, Edges> compile(Collection<InputGraph> graph)
            throws CompilerGraphException;

    /* creates an input graph with MD-SAL binding */
    InputGraph createGraph(Nodes source, Nodes destination, Set<Edges> action);
}
