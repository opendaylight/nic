/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.samples.impl;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ActionProvider.class);

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("ActionProvider Session Initiated");
    }

    @Override
    public void close() throws Exception {
        LOG.info("ActionProvider Closed");
    }

}
