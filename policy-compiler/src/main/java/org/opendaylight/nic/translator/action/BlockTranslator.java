//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.translator.action;

import java.util.HashSet;
import java.util.Set;

import org.opendaylight.nic.extensibility.ActionLabel;
import org.opendaylight.nic.extensibility.actions.BlockActionType;
import org.opendaylight.nic.intent.AuxiliaryData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Instruction;


public class BlockTranslator extends BaseActionTranslator {

    @Override
    public Set<Instruction> translate(AuxiliaryData aux) {
        // Empty action list implies drop
        return new HashSet<>();
    }

    @Override
    public ActionLabel actionLabel() {
        return BlockActionType.getInstance().label();
    }

}
