package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.common.rev160530;

import org.opendaylight.nic.transaction.impl.IntentCommonProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntentCommonProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.common.rev160530.AbstractIntentCommonProviderModule {
    public IntentCommonProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }
    private static final Logger LOG = LoggerFactory.getLogger(IntentCommonProviderModule.class);

    public IntentCommonProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.common.rev160530.IntentCommonProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("Starting Intent-Common module.");
        final IntentCommonProviderImpl commonProvider = new IntentCommonProviderImpl();
        commonProvider.start();
        return commonProvider;
    }

}
