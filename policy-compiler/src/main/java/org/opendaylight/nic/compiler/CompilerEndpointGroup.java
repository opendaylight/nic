//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import org.opendaylight.nic.compiler.parser.PrefixExpression;
import org.opendaylight.nic.intent.Endpoint;
import org.opendaylight.nic.intent.EndpointGroup;
import org.opendaylight.nic.intent.impl.EndpointGroupImpl;

/**
 * An implementation of {@link EndpointGroup} which provides specific
 * functionality related to the policy compiler.
 *
 * @author Duane Mentze
 */
public class CompilerEndpointGroup extends EndpointGroupImpl {

    private final PrefixExpression expression;

    public boolean isAll() {
        return expression.isAll();
    }

    public boolean isMember(Endpoint endpoint) {
        return expression.isMember(endpoint);
    }

    public CompilerEndpointGroup and(CompilerEndpointGroup other) {
        EndpointGroupImpl epg = super.and(other);
        PrefixExpression pe = expression.and(other.expression);
        return new CompilerEndpointGroup(epg, pe);
    }

    public CompilerEndpointGroup andNot(CompilerEndpointGroup other) {
        EndpointGroupImpl epg = super.andNot(other);
        PrefixExpression notOther = other.expression.not();
        PrefixExpression pe = expression.and(notOther);
        return new CompilerEndpointGroup(epg, pe);
    }

    public static CompilerEndpointGroup getInstance(EndpointGroup group) {
        return new CompilerEndpointGroup(group);
    }

    private CompilerEndpointGroup(EndpointGroup group) {
        super(group.group());
        expression = new PrefixExpression(group);
    }

    private CompilerEndpointGroup(EndpointGroupImpl group,
            PrefixExpression expression) {
        super(group);
        this.expression = new PrefixExpression(expression);
    }

    private CompilerEndpointGroup(EndpointGroup group,
            PrefixExpression expression) {
        super(group.group());
        this.expression = new PrefixExpression(group);
    }

    @Override
    public String toString() {
        return "EndpointGroupImpl [" + expression.prefixToInfix() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((expression == null) ? 0 : expression.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompilerEndpointGroup other = (CompilerEndpointGroup) obj;
        if (expression == null) {
            if (other.expression != null)
                return false;
        } else if (!expression.equals(other.expression))
            return false;
        return true;
    }

}
