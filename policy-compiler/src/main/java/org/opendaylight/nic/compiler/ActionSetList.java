//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import java.util.List;

/**
 * List of {@link ActionSet}s. These are typically used in combining policies,
 * to give ordering to the priority in which {@link ActionSet}s should apply for
 * a given {@link CompiledPolicy}.
 *
 * @author Duane Mentze
 *
 */
public interface ActionSetList {

    /**
     * Returns the list of action sets.
     *
     * @return list of action sets
     */
    public List<ActionSet> getList();

    /**
     * Composes this {@link ActionSetList} with the given {@link ActionSetList}.
     *
     * @param list
     *            the given action set list
     * @return a combined action set list
     */
    public ActionSetList compose(ActionSetList list);

    /**
     * Returns whether or not all action sets in this list are observers.
     *
     * @return whether all are observers
     */
    public boolean isObserver();

    /**
     * Returns the maximum action precedence contained in this action set list.
     *
     * @return maximum action precedence
     */
    public long maxActionPrecedence();

    /**
     * Returns whether or not all action sets in this list are composable.
     *
     * @return whether all are composable
     */
    public boolean isComposable();

}
