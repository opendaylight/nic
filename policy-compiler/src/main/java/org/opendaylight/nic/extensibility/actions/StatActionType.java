//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.extensibility.actions;

import org.opendaylight.nic.extensibility.ActionLabel;

/**
 * Gathers statistics on matching traffic.
 *
 * @author Shaun Wackerly
 */
public class StatActionType extends BaseActionType {

    private static final ActionLabel STAT = new ActionLabel("stat");
    private static final StatActionType INSTANCE = new StatActionType(
            DEFAULT_PRECEDENCE);

    public static StatActionType getInstance() {
        return INSTANCE;
    }

    private StatActionType(long precedence) {
        super(STAT, true, false, true, precedence);
    }

}
