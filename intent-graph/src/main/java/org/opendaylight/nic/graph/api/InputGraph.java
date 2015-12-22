/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.api;

import org.opendaylight.nic.graph.impl.ClassifierImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Nodes;

import java.util.Set;

/**
 * The input graph is created with MD-SAL Binding
 */
public interface InputGraph {

    /**
     * The source node with a label string that identifies itself part of an End-point group
     * Contructor
     * @return Nodes
     */
    Set<Nodes> src();

    /**
     * The destination node with a label string that identifies itself part of an End-point group
     * Contructor
     * @return Nodes
     */
    Set<Nodes> dst();

    /**
     * The edge is a representation of the source and destination node and the action associated to it.
     * @return Edges
     */
    Set<Edges> action();

    /**
     * The classifier describes the application and port IDs
     * @return Classifier
     */
    // TODO Use the classifier from yang model
    ClassifierImpl classifier();
}

