package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.statemachine.rev150507;

import org.opendaylight.nic.engine.IntentStateMachineExecutorService;
import org.opendaylight.nic.impl.IntentStateMachineExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntentStateMachineProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.statemachine.rev150507.AbstractIntentStateMachineProviderModule {
    private static final Logger LOG = LoggerFactory.getLogger(IntentStateMachineProviderModule.class);
    public IntentStateMachineProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public IntentStateMachineProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.statemachine.rev150507.IntentStateMachineProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("Creating Intent State Machine");
        final IntentStateMachineExecutorService executorService = new IntentStateMachineExecutor(getDataBrokerDependency());
        executorService.init();
        return executorService;
    }

}
