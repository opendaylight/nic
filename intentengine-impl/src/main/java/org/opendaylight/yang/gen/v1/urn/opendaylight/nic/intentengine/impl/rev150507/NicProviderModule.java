package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intentengine.impl.rev150507;

import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.nic.impl.NicProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NicProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intentengine.impl.rev150507.AbstractNicProviderModule {

    private static final Logger LOG = LoggerFactory.getLogger(NicProviderModule.class);

    public NicProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public NicProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intentengine.impl.rev150507.NicProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("Creating a new NicProvider instance");
        // final NicProvider provider = new NicProvider();

        // TODO: Add null pointer checking
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<NicConsoleProvider> serviceReference = bundleContext.getServiceReference(NicConsoleProvider.class);
        NicConsoleProvider service = bundleContext.getService(serviceReference);
        return service;
        // FIXME: MD-SAL should not manage the service lifecycle

        // provider.init(getDataBrokerDependency());

        // return provider;
    }

}
