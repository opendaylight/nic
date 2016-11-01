package org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.nic.impl.IntentEngineProviderImpl;

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
        final DataBroker dataBroker = getDataBrokerDependency();
        final IntentEngineProviderImpl commonProvider = new IntentEngineProviderImpl(dataBroker);
        try {
            commonProvider.init();
        } catch (TransactionCommitFailedException e) {
            e.printStackTrace();
        }
        return commonProvider;
    }

}
