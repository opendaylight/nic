//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler.api;

/*
 * A label which designates a specific term type. This class is a type-safe
 * representation which contains a single String.
 * 
 */
public class TermLabel {

    private final String label;

    /**
     * Constructs a term type label from the given label.
     * 
     * @param label
     *            term type label
     */
    public TermLabel(String label) {
        if (label == null)
            throw new NullPointerException("Cannot create label from null");
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TermLabel other = (TermLabel) obj;
        return label.equals(other.label);
    }

}
