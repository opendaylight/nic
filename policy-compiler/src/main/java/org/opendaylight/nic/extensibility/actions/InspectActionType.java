//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.extensibility.actions;

import org.opendaylight.nic.extensibility.ActionLabel;
import org.opendaylight.nic.intent.AuxiliaryData;

/**
 * Redirect packets to an inspection service on their way to the final
 * destination.
 *
 * @author Duane Mentze
 * @author Shaun Wackerly
 */
public class InspectActionType extends BaseActionType {

    private static final ActionLabel INSPECT = new ActionLabel("inspect");
    private static final InspectActionType INSTANCE = new InspectActionType(
            DEFAULT_PRECEDENCE);

    public static InspectActionType getInstance() {
        return INSTANCE;
    }

    private InspectActionType(long precedence) {
        super(INSPECT, false, true, true, precedence);
    }

    @Override
    public boolean validate(AuxiliaryData data) {
        // TODO Auto-generated method stub
        return false;
    }

}
