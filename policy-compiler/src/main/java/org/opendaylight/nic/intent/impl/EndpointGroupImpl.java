//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent.impl;

import org.opendaylight.nic.compiler.parser.PrefixExpression;
import org.opendaylight.nic.intent.EndpointGroup;

/**
 * An attribute-based logical expression that can be evaluated to create a set
 * of endpoints.
 * 
 * @author Duane Mentze
 */
public class EndpointGroupImpl implements EndpointGroup {

    private final String group;

    @Override
    public String group() {
        return group;
    }

    public EndpointGroupImpl and(EndpointGroupImpl other) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(String.format("( %s ) and ( %s) )", group, other.group()));
        return new EndpointGroupImpl(new String(buffer), false);
    }

    public EndpointGroupImpl andNot(EndpointGroupImpl other) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(String.format("( %s ) and not ( %s) )", group,
                other.group()));
        return new EndpointGroupImpl(new String(buffer), false);
    }

    private EndpointGroupImpl(String group, boolean validate) {
        if (validate) {
            validate(group);
        }
        this.group = group;
    }

    public EndpointGroupImpl(EndpointGroupImpl group) {
        this.group = group.group();
    }

    public EndpointGroupImpl(EndpointGroup group) {
        this.group = group.group();
    }

    public EndpointGroupImpl(String group) {
        validate(group);
        this.group = group;
    }

    private void validate(String group) {
        if (group == null)
            throw new NullPointerException("Cannot create group from null");
        if (!PrefixExpression.isValid(group)) {
            throw new IllegalArgumentException("not a valid expression");
        }
    }

    @Override
    public String toString() {
        return group;
    }

    @Override
    public int hashCode() {
        return group.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EndpointGroup other = (EndpointGroup) obj;
        return group.equals(other.group());
    }

}
