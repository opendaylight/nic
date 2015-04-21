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
 * Send a <U>copy</U> of the packets to a service function.
 *
 * @author Duane Mentze
 */
public class AuditActionType extends BaseActionType {

    private static final ActionLabel AUDIT = new ActionLabel("audit");
    private static final AuditActionType INSTANCE = new AuditActionType(
            DEFAULT_PRECEDENCE);

    public static AuditActionType getInstance() {
        return INSTANCE;
    }

    public AuditActionType(long precedence) {
        super(AUDIT, true, true, true, precedence);
    }

    @Override
    public boolean validate(AuxiliaryData data) {
        // TODO Auto-generated method stub
        return false;
    }

}
