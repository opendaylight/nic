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
 * Guarantee a minimum level of network latency for matching traffic.
 *
 * @author Duane Mentze
 * @author Shaun Wackerly
 */
public class LatencyActionType extends BaseActionType {

    private static final ActionLabel LATENCY = new ActionLabel("latency");
    private static final LatencyActionType INSTANCE = new LatencyActionType(
            DEFAULT_PRECEDENCE);

    public static LatencyActionType getInstance() {
        return INSTANCE;
    }

    private LatencyActionType(long precedence) {
        super(LATENCY, false, true, false, precedence);
    }

    @Override
    public int resolveDuplicate(AuxiliaryData a, AuxiliaryData b) {
        LatencyActionData la = (LatencyActionData) a;
        LatencyActionData lb = (LatencyActionData) b;

        // Return the data with the higher DSCP value
        if (la.dscp() > lb.dscp())
            return 1;
        return -1;
    }

    @Override
    public boolean validate(AuxiliaryData data) {
        // TODO Auto-generated method stub
        return false;
    }

}
