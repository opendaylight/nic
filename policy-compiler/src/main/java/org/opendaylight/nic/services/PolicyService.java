//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.services;

import org.opendaylight.nic.common.AppId;
import org.opendaylight.nic.intent.Policy;
import org.opendaylight.nic.intent.PolicyStatus;


/**
 * Provides a mechanism for management of policies.
 *
 * @author Duane Mentze
 */
public interface PolicyService {

    /**
     * Adds the given policy to apply to policy domain.
     *
     * @param policy to be added
     */
    PolicyStatus add(Policy policy, AppId app);

    /**
     * Updates the policy in policy domain.
     *
     * @param policy to be removed
     */
    PolicyStatus update(Policy current, Policy updated, AppId app);

    /**
     * Removes the policy from the policy domain.
     *
     * @param policy to be removed
     */
    void remove(Policy policy, AppId app);

}

