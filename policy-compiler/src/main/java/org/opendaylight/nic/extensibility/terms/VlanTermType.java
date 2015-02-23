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
 * Matches traffic on a specific vlan.
 *
 * @author Shaun Wackerly
 */
public class VlanTermType extends TermTypeBase {

	private final static int VLAN_MIN = 0;
	private final static int VLAN_MAX = 4095;
	private final static VlanTermType INSTANCE = new VlanTermType();

	public static VlanTermType getInstance() {
		return INSTANCE;
	}

	private VlanTermType() {
		super(new TermLabel("VLAN"), VLAN_MIN, VLAN_MAX);
	}

}
