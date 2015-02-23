//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent.impl;




import java.util.HashMap;
import java.util.Map;

import org.opendaylight.nic.extensibility.ActionTranslator;
import org.opendaylight.nic.intent.AuxiliaryData;

/**
 * The common interface for all auxiliary data given to both {@link ActionTranslator}
 * objects to use during translation and the Policy Compiler during compilation.
 *
 * @author Duane Mentze
 */
public class AuxiliaryDataImpl extends HashMap<String,String>
                               implements AuxiliaryData {

    private static final long serialVersionUID = 4979074076410262102L;

    /** Returns the set of name-value pairs associated with this data. */
	@Override
	public Map<String,String> getData() {
		return this;
	}

}
