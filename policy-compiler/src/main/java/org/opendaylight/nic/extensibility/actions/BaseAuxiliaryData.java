//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.extensibility.actions;

import java.util.HashMap;
import java.util.Map;

import org.opendaylight.nic.intent.AuxiliaryData;

/**
 * Base implementation for {@link AuxiliaryData}.
 *
 * @author Shaun Wackerly
 */
@SuppressWarnings("serial")
public class BaseAuxiliaryData extends HashMap<String, String> implements
        AuxiliaryData {

    @Override
    public Map<String, String> getData() {
        return this;
    }

}
