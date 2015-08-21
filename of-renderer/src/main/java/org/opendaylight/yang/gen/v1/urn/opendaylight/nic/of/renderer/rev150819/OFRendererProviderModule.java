package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.of.renderer.rev150819;

import nic.of.renderer.OFRendererProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OFRendererProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.of.renderer.rev150819.AbstractOFRendererProviderModule {
    private static final Logger LOG = LoggerFactory.getLogger(OFRendererProviderModule.class);

    public OFRendererProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public OFRendererProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.of.renderer.rev150819.OFRendererProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("Creating Open flow renderer");
        final OFRendererProvider provider = new OFRendererProvider(getDataBrokerDependency());
        provider.init();
        return provider;
    }

}
