/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.GraphEdgeAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;

import java.sql.Timestamp;
import java.util.Date;

public class GraphEdgeAddedImpl implements GraphEdgeAdded {
    private Edges edge;
    private final Timestamp timeStamp;

    public GraphEdgeAddedImpl(Edges edge) {
        this.edge = edge;
        Date date= new Date();
        timeStamp = new Timestamp(date.getTime());
    }

    @Override
    public Edges getEdge() { return edge; }

    @Override
    public Timestamp getTimeStamp() {
        return timeStamp;
    }
}
