//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.extensibility.terms;

import org.opendaylight.nic.extensibility.TermLabel;

/**
 * Matches traffic with a particular L4 source port value.
 *
 * @author Duane Mentze
 * @author Shaun Wackerly
 */
public class L4SrcTermType extends TermTypeBase {

	private final static int L4_SRC_MIN = 0;
	private final static int L4_SRC_MAX = 65535;
	private final static L4SrcTermType INSTANCE = new L4SrcTermType();

	public static L4SrcTermType getInstance() {
		return INSTANCE;
	}

	private L4SrcTermType() {
		super(new TermLabel("L4_SRC"), L4_SRC_MIN, L4_SRC_MAX);
	}

}
