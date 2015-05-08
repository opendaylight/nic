/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.vtnrender.impl;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;


public class VTNRenderProvider implements BindingAwareProvider,AutoCloseable {
    /**
     * Logger instance.
     */
    private static final Logger LOG = LoggerFactory.getLogger(VTNRenderProvider.class);
    /**
     * Data broker SAL service.
     */
    private DataBroker dataBroker;
   /**
     * {@inheritDoc}
     */
    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("NicProvider Session Initiated");

        this.dataBroker = session.getSALService(DataBroker.class);
        ConfigListener configListener = new ConfigListener(dataBroker);

    }
   /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws Exception {
        LOG.info("NicProvider Closed");
    }
}
