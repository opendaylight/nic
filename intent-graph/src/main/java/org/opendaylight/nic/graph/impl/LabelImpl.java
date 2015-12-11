/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import com.google.common.base.Preconditions;

import java.util.Set;
import java.util.HashSet;

public class LabelImpl{
    protected String parent;
    protected Set<String> children;
    protected NodeImpl node;

    public LabelImpl(String parent, String child, NodeImpl node) {
        this.parent = parent;
        if (child != null) {
            this.children.add(child);
        }
        if (Preconditions.checkNotNull(node) != null) {
            this.node = node;
        }
        
    }

    public LabelImpl(String parent, String[] children, NodeImpl node) {
        this.parent = parent;
        for (String child : children) {
            if (child != null) {
                this.children.add(child);
            }
        }
        if (Preconditions.checkNotNull(node) != null) {
            this.node = node;
        }
    }

    public String getParent() {
        return parent;
    }

    public Set<String> getChildren() {
        return children;
    }

    public void addChild(String child) {
        this.children.add(child);
    }

    public void addChildren(String[] children) {
        for (String child : children) {
            if (child != null) {
                this.children.add(child);
            }
        }
    }
    //TODO: add methods to delete children
}
