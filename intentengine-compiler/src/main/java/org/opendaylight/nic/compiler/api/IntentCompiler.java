/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.compiler.api;

import java.util.Collection;
import java.util.Set;

import org.opendaylight.nic.compiler.Edge;
import org.opendaylight.nic.compiler.Epg;

import edu.uci.ics.jung.graph.DirectedGraph;

public interface IntentCompiler {
    DirectedGraph<Epg, Edge> compile(Collection<Policy> policies)
            throws IntentCompilerException;

    // Set<Endpoint> parseEndpointGroup(String csv) throws UnknownHostException;

    Policy createPolicy(Epg source, Epg destination, Set<Action> action);
}
