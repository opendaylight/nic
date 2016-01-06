/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.neutron.integration.rev151125;

import org.opendaylight.nic.listeners.api.EventRegistryService;
import org.opendaylight.nic.neutron.integration.impl.NeutronIntegrationProviderImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
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
        // Retrieve reference for Event Registry service
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<?> serviceReference = context.
                getServiceReference(EventRegistryService.class);
        EventRegistryService serviceRegistry = (EventRegistryService) context.
                getService(serviceReference);
        final NeutronIntegrationProviderImpl provider =
                new NeutronIntegrationProviderImpl(getDataBrokerDependency(), serviceRegistry, getIntentImplDependency());
        provider.start();
        LOG.info("Neutron integration bundle started successfully.");

        return provider;
    }
}

