/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.neutron.integration.rev151125;

import org.opendaylight.nic.neutron.integration.impl.NeutronIntegrationProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NeutronIntegrationProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.neutron.integration.rev151125.AbstractNeutronIntegrationProviderModule {
    private static final Logger LOG = LoggerFactory.getLogger(NeutronIntegrationProviderModule.class);

    public NeutronIntegrationProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public NeutronIntegrationProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.neutron.integration.rev151125.NeutronIntegrationProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final NeutronIntegrationProviderImpl provider = new NeutronIntegrationProviderImpl(getDataBrokerDependency());
        provider.start();
        LOG.info("Neutron integration bundle started successfully.");

        return new AutoCloseable() {

            @Override
            public void close() throws Exception {
                try {
                    provider.close();
                } catch (final Exception e) {
                    LOG.error("Unexpected error while stopping NeutronIntegrationProviderModule", e);
                }
                LOG.info("NeutronIntegrationProviderModule stopped.");
            }
        };
    }
}

