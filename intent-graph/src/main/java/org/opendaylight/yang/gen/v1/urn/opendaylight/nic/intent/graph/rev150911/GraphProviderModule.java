package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911;

import org.opendaylight.nic.graph.impl.CompilerGraphImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.AbstractGraphProviderModule {
    private static final Logger LOG = LoggerFactory
            .getLogger(CompilerGraphImpl.class);
    public GraphProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public GraphProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.GraphProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("Creating Graph instance");
        final CompilerGraphImpl provider = new CompilerGraphImpl();
        provider.init();
        return provider;
    }

}
