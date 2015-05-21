//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------

package org.opendaylight.nic.impl.actions;

import org.opendaylight.nic.compiler.api.Action;
import org.opendaylight.nic.compiler.api.ActionType;

public class Block implements Action {
    @Override
    public String getName() {
        return "BLOCK";
    }

    @Override
    public ActionType getType() {
        return ActionType.EXCLUSIVE;
    }

    @Override
    public String toString() {
        return getName();
    }
}
