//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

/**
 * A set of {@link Action}s which can be operated upon as a common set.
 *
 * @author Duane Mentze
 */

import java.util.Set;

import org.opendaylight.nic.intent.Policy;

public interface ActionSet extends Comparable<ActionSet> {

    /**
     * Returns set of actions in action set.
     */
    Set<CompilerAction> getActions();

    /**
     * Returns true if all actions are composable, otherwise false.
     */
    public boolean isComposable();

    /**
     * Returns true if all actions are observer, otherwise false.
     */
    public boolean isObserver();

    /**
     * Returns the policy which originated the actionSet.
     */
    public Policy policy();

    /**
     * Returns max action precedence in the set
     */
    public long maxActionPrecedence();

}
