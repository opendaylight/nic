//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.extensibility;

import org.opendaylight.nic.intent.AuxiliaryData;
import org.opendaylight.nic.services.PolicyFramework;

/**
 * An action type which becomes the reference behind a {@link ActionLabel}. The
 * {@link ActionLabel} is the user-facing label which corresponds to this
 * internally-used type. This type must be registered with the
 * {@link PolicyFramework} prior to use.
 *
 * @author Shaun Wackerly
 */
public interface ActionType {

    /** Default action precedence. */
    public static final long DEFAULT_PRECEDENCE = 0;

    /**
     * Returns the label associated with this action.
     *
     * @return action label
     */
    ActionLabel label();

    /**
     * Returns the user-readable name of this action.
     *
     * @return action name
     */
    String readableName();

    /**
     * Returns whether or not this action is considered an <i>observer</i>.
     *
     * @return true if observer, false if not
     */
    boolean isObserver();

    /**
     * Returns whether or not this action is considered <i>composable</i>.
     *
     * @return true if composable, false if not
     */
    boolean isComposable();

    /**
     * Returns whether or not this action is allowed to have duplicates when it
     * is compiled. For instance, an action which sets a minimum bandwidth
     * should not have any duplicates because only one minimum bandwidth can be
     * guaranteed.
     *
     * @return true if duplicates are allowed, false if not
     */
    boolean allowDuplicate();

    /**
     * Returns this action's precedence value, which can be used to order this
     * action in relation to other actions.
     *
     * @return action precedence
     */
    long precedence();

    /**
     * Resolves duplicate actions by choosing one to supercede the other:
     * <ul>
     * <li>1: for a
     * <li>-1: for b
     * <li>0: for either
     * </ul>
     *
     * @return an integer which indicates the superceding action
     */
    int resolveDuplicate(AuxiliaryData a, AuxiliaryData b);

    /**
     * Validates the given auxiliary data.
     *
     * @return true if valid, false if not
     */
    boolean validate(AuxiliaryData data);
}
