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
 * Redirect packets to a service chain, without forwarding the traffic
 * to its final destination.
 *
 * @author Duane Mentze
 * @author Shaun Wackerly
 */
public class RedirectActionType extends BaseActionType {

	private static final long REDIRECT_PRECEDENCE = 9;
    private static final ActionLabel REDIRECT = new ActionLabel("redirect");
	private static final RedirectActionType INSTANCE = new RedirectActionType(REDIRECT_PRECEDENCE);

	public static RedirectActionType getInstance() {
		return INSTANCE;
	}

    private RedirectActionType(long precedence) {
        super(REDIRECT, false, false, true, precedence);
    }

    @Override
    public boolean validate(AuxiliaryData data) {
        // TODO Auto-generated method stub
        return false;
    }

}
