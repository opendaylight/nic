/*
 * Copyright (c) 2016 Open Networking Foundation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package service;

import org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.modules.module.configuration.intent.nbi.DataBroker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntentEngineProviderServiceImpl implements AutoCloseable{
    private static final Logger LOG = LoggerFactory
            .getLogger(IntentEngineProviderServiceImpl.class);
    private ServiceRegistration<IntentEngineProviderServiceImpl> engineRegistration;

    public void init() {
        try {
            BundleContext context =
                    FrameworkUtil.getBundle(this.getClass()).getBundleContext();
            engineRegistration = context.registerService(IntentEngineProviderServiceImpl.class, this, null);
        } catch (Exception e) {
            LOG.error("Exception in IntentEngineProviderService");
        }
        LOG.info("Initialization done");
    }

    @Override
    public void close() throws Exception {
        engineRegistration.unregister();
    }
}
