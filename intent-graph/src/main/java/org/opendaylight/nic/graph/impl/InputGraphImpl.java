/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import org.opendaylight.nic.graph.api.InputGraph;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.IntentIds;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Nodes;

import java.util.Set;

public class InputGraphImpl implements InputGraph {

    protected final Set<IntentIds> id;
    protected final Set<Nodes> src;
    protected final Set<Nodes> dst;
    protected final Set<Edges> action;
    protected final ClassifierImpl classifier;

    /* based on the description of nodes and edges, the edge can describe a complete intent */

    public InputGraphImpl(Set<IntentIds> id, Set<Nodes> src, Set<Nodes> dst, Set<Edges> action) {
        this.id = id;
        this.src = src;
        this.dst = dst;
        this.action = action;
        this.classifier = null;
    }

    public InputGraphImpl(Set<Nodes> src, Set<Nodes> dst, Set<Edges> action) {
        this.id = null;
        this.src = src;
        this.dst = dst;
        this.action = action;
        this.classifier = null;
    }

    public InputGraphImpl(Set<Nodes> src, Set<Nodes> dst, Set<Edges> action, ClassifierImpl classifier) {
        this.id = null;
        this.src = src;
        this.dst = dst;
        this.action = action;
        this.classifier = classifier;
    }

    @Override
    public Set<IntentIds> id() {
        return id;
    }

    @Override
    public Set<Nodes> src() {
        return src;
    }

    @Override
    public Set<Nodes> dst() {
        return dst;
    }

    @Override
    public Set<Edges> action() {
        return action;
    }

    @Override
    public ClassifierImpl classifier() {
        return classifier;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        InputGraphImpl graph = (InputGraphImpl) object;

        if (src != null ? !src.equals(graph.src) : graph.src != null) {
            return false;
        }
        if (dst != null ? !dst.equals(graph.dst) : graph.dst != null) {
            return false;
        }
        if (action != null ? !action.equals(graph.action) : graph.action != null) {
            return false;
        }
        if (classifier == null) {
            if (graph.classifier != null) {
                return false;
            }
        } else if (!classifier.equals(graph.classifier)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = src != null ? src.hashCode() : 0;
        result = prime * result + (dst != null ? dst.hashCode() : 0);
        result = prime * result + (action != null ? action.hashCode() : 0);
        result = 31 * result + (classifier != null ? classifier.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("from %s to %s apply %s when %s", src, dst, action, classifier);
    }
}
