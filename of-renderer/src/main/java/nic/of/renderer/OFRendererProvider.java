/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package nic.of.renderer;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by saket on 8/19/15.
 */
public class OFRendererProvider implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(OFRendererProvider.class);

    private DataBroker dataBroker;

    protected ServiceRegistration nicConsoleRegistration;

    private OFRendererDataChangeListener ofRendererDataChangeListener;

    public OFRendererProvider(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void init() {
        LOG.info("OF Renderer Provider Session Initiated");

        // Initialize operational and default config data in MD-SAL data store
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        nicConsoleRegistration = context.registerService(OFRendererProvider.class, this, null);

        ofRendererDataChangeListener = new OFRendererDataChangeListener(dataBroker);
    }
    @Override
    public void close() throws Exception {

    }
}
