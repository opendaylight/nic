//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent;

import org.opendaylight.nic.extensibility.ActionLabel;

/**
 * The action which should be applied to packets which match a given policy. An
 * action has a label, which is registered with the {@link PolicyFramework} and
 * then used in a {@link Policy}. The action also has {@link AuxiliaryData}
 * which is supplied by the application. The meaning of an action (and its
 * label) is defined by the application who registers the label and interprets
 * the auxiliary data.
 *
 * @author Shaun Wackerly
 * @author Duane Mentze
 */

public interface Action {

    /**
     * Returns the action label associated with this action.
     *
     * @return action label
     */
    ActionLabel label();

    /**
     * Returns the auxiliary data associated with this action.
     *
     * @return auxiliary data
     */
    AuxiliaryData data();
}
