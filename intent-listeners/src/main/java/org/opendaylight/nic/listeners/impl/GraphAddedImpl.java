/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;


import org.opendaylight.nic.listeners.api.GraphAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.Graph;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class GraphAddedImpl implements GraphAdded {
    private Graph graph;
    private List<Edges> edges;
    private final Timestamp timeStamp;

    public GraphAddedImpl(Graph graph) {
        this.graph = graph;
        this.edges = graph.getEdges();
        Date date= new Date();
        timeStamp = new Timestamp(date.getTime());
    }

    @Override
    public Graph getGraph() { return graph; }

    @Override
    public List<Edges> getEdge() { return edges; }

    @Override
    public Timestamp getTimeStamp() {
        return timeStamp;
    }
}
