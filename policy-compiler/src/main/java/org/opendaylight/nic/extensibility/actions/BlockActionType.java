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
 * Blocks packets from being forwarded to their destination.
 *
 * @author Duane Mentze
 * @author Shaun Wackerly
 */
public class BlockActionType extends BaseActionType {

	private static final long BLOCK_PRECEDENCE = 10;
    private static final ActionLabel BLOCK = new ActionLabel("block");
	private static final BlockActionType INSTANCE = new BlockActionType(BLOCK_PRECEDENCE);

	public static BlockActionType getInstance() {
		return INSTANCE;
	}

    public BlockActionType(long precedence) {
        super(BLOCK, false, false, true, precedence);
    }

    @Override
    public int resolveDuplicate(AuxiliaryData a, AuxiliaryData b) {
        throw new IllegalStateException("Cannot resolve duplicate");
    }

}
