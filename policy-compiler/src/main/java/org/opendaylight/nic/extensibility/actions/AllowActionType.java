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
 * Allows packets to be forwarded normally.
 *
 * @author Duane Mentze
 */
public class AllowActionType extends BaseActionType {

    private static final ActionLabel ALLOW = new ActionLabel("allow");
	private static final AllowActionType INSTANCE = new AllowActionType(DEFAULT_PRECEDENCE);

    private AllowActionType(long precedence) {
        super(ALLOW, false, true, false, precedence);
    }

	public static AllowActionType getInstance() {
		return INSTANCE;
	}

    @Override
    public int resolveDuplicate(AuxiliaryData a, AuxiliaryData b) {
        return 0;
    }

}
