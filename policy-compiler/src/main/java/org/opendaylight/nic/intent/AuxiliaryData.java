//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent;

import java.util.Map;

import org.opendaylight.nic.extensibility.ActionTranslator;
import org.opendaylight.nic.services.PolicyService;

/**
 * The common interface for all auxiliary data given to both {@link ActionTranslator}
 * objects to use during translation and the {@link PolicyService} during compilation.
 * This data accompanies and is specific to an {@link ActionType}.
 *
 * @author Shaun Wackerly
 */
public interface AuxiliaryData {

    /**
     * Returns the set of name-value pairs associated with this data.
     *
     * @return name-value pairs
     */
    Map<String,String> getData();

}
