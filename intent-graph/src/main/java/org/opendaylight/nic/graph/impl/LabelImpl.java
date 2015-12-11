/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import java.util.Set;

public class LabelImpl{
    protected String parent;
    protected Set<String> children;
    protected NodeImpl node;

    public LabelImpl(String parent, String child) {
        this.parent = parent;
        if (child != null) {
            this.children.add(child);
        }
        this.node = null;

    }
    public LabelImpl(String parent, String[] children) {
        this.parent = parent;
        for (String child : children) {
            if (child != null) {
                this.children.add(child);
            }
        }
        this.node = null;
    }

    public LabelImpl(String parent, String child, NodeImpl node) {
        this.parent = parent;
        if (child != null) {
            this.children.add(child);
        }
        if(node != null) {
            this.node = node;
        }
        else {
            this.node = null;
        }
    }

    public LabelImpl(String parent, String[] children, NodeImpl node) {
        this.parent = parent;
        for (String child : children) {
            if (child != null) {
                this.children.add(child);
            }
        }
        if(node != null) {
            this.node = node;
        }
        else {
            this.node = null;
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
