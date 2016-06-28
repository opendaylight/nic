package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.common.rev160530;

import transaction.impl.IntentCommonProviderImpl;

public class IntentCommonProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.common.rev160530.AbstractIntentCommonProviderModule {
    public IntentCommonProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public IntentCommonProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.common.rev160530.IntentCommonProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final IntentCommonProviderImpl commonProvider = new IntentCommonProviderImpl();
        commonProvider.start();
        return commonProvider;
    }

}
