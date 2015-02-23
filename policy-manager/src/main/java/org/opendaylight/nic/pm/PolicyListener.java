//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.pm;

import java.util.Set;

import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.controller.config.policy.manager.impl.rev141208.domains.domain.application.app.policy.Policy;

/**
 * Listens to changes in the operational state of policies.
 *
 * @author Shaun Wackerly
 */
public interface PolicyListener {

    /**
     * Notifies this listener of a change to the operational state of policies,
     * with the change designated by created, updated, and removed policies.
     */
    void notifyChange(Set<Policy> created, Set<Policy> updated,
            Set<Policy> removed);
}
