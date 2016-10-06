package org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.IntentEngineProviderServiceImpl;

public class IntentEngineProviderModule extends org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.AbstractIntentEngineProviderModule {
    private static final Logger LOG = LoggerFactory
            .getLogger(IntentEngineProviderModule.class);
    public IntentEngineProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public IntentEngineProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.IntentEngineProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("Starting Intent-Engine module.");
        final IntentEngineProviderServiceImpl commonProvider = new IntentEngineProviderServiceImpl();
        commonProvider.init();
        return commonProvider;
    }

}
