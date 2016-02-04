/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.GraphEdgeUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.Graph;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class GraphEdgeUpdatedImpl implements GraphEdgeUpdated {
    private Set<Edges> edges;
    private final Timestamp timeStamp;

    public GraphEdgeUpdatedImpl(Graph graph) {
        this.edges = new HashSet<Edges>(graph.getEdges());
        Date date= new Date();
        timeStamp = new Timestamp(date.getTime());
    }

    public GraphEdgeUpdatedImpl(Set<Edges> edges) {
        this.edges = edges;
        Date date= new Date();
        timeStamp = new Timestamp(date.getTime());
    }

    @Override
    public Set<Edges> getEdge() { return edges; }

    @Override
    public Timestamp getTimeStamp() {
        return timeStamp;
    }
}
