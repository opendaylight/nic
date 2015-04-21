//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------

package org.opendaylight.nic.compiler.impl;

import java.util.HashSet;
import java.util.Set;

import org.opendaylight.nic.compiler.ActionSet;
import org.opendaylight.nic.compiler.CompilerAction;
import org.opendaylight.nic.intent.Action;
import org.opendaylight.nic.intent.Policy;

/**
 * Set of {@link Action}s for a {@link Policy}
 *
 * @author Duane Mentze
 */
public class ActionSetImpl implements ActionSet, Comparable<ActionSet> {

    private Set<CompilerAction> actions;
    private Policy policy;
    private final long maxActionPrecedence;

    @Override
    public Set<CompilerAction> getActions() {
        return actions;
    }

    @Override
    public Policy policy() {
        return policy;
    }

    public ActionSetImpl(Set<CompilerAction> actions, Policy policy) {
        super();

        this.actions = new HashSet<CompilerAction>(actions);
        this.policy = policy;
        this.maxActionPrecedence = calcMaxActionPrecedence();
    }

    /**
     * Returns true if all actions are composable, otherwise false.
     */
    @Override
    public boolean isComposable() {
        for (CompilerAction a : actions) {
            if (!a.isComposable()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if all actions are observer, otherwise false.
     */
    @Override
    public boolean isObserver() {
        for (CompilerAction a : actions) {
            if (!a.isObserver()) {
                return false;
            }
        }
        return true;
    }

    private long calcMaxActionPrecedence() {
        long max = 0;
        for (CompilerAction a : actions) {
            if (a.precedence() > max) {
                max = a.precedence();
            }
        }
        return max;
    }

    /**
     * Returns max precedence of all Actions
     */
    @Override
    public long maxActionPrecedence() {
        return maxActionPrecedence;
    }

    // @Override
    public int compareTo(ActionSet o) {
        if (this.policy().application().priority() < o.policy().application()
                .priority()) {
            return -1;
        }
        if (this.policy().application().priority() > o.policy().application()
                .priority()) {
            return 1;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((actions == null) ? 0 : actions.hashCode());
        result = prime * result
                + (int) (maxActionPrecedence ^ (maxActionPrecedence >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ActionSetImpl other = (ActionSetImpl) obj;
        if (actions == null) {
            if (other.actions != null)
                return false;
        } else if (!actions.equals(other.actions))
            return false;
        if (maxActionPrecedence != other.maxActionPrecedence)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ActionSetImpl { Actions {" + actions + "} policy="
                + policy.name() + ", maxActionPrecedence="
                + maxActionPrecedence + "}";
    }
}
