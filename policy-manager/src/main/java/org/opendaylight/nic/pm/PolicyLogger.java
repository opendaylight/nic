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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs all policy-related changes to the system log.
 *
 * @author Shaun Wackerly
 */
public class PolicyLogger implements AutoCloseable, PolicyListener {

    private static final Logger log = LoggerFactory
            .getLogger(PolicyLogger.class);

    private final PolicyNotifier notifier;

    /**
     * Creates a PolicyLogger which registers itself with the given notifier.
     *
     * @param notifier
     *            notifies of policy changes
     */
    public PolicyLogger(PolicyNotifier notifier) {
        this.notifier = notifier;
        notifier.registerListener(this);
    }

    @Override
    public void notifyChange(Set<Policy> created, Set<Policy> updated,
            Set<Policy> removed) {
        for (Policy p : created)
            log.info("Policy CREATED: " + p);

        for (Policy p : updated)
            log.info("Policy UPDATED: " + p);

        for (Policy p : removed)
            log.info("Policy REMOVED: " + p);
    }

    @Override
    public void close() throws Exception {
        notifier.unregisterListener(this);
    }

}
