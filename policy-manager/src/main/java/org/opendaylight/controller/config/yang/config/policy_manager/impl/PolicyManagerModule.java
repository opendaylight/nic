package org.opendaylight.controller.config.yang.config.policy_manager.impl;

import org.opendaylight.controller.config.spi.Module;
import org.opendaylight.nic.pm.NetworkIntentCompilerTestFramework;
import org.opendaylight.nic.pm.PolicyLogger;
import org.opendaylight.nic.pm.PolicyNotifier;
import org.opendaylight.nic.pm.PolicyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolicyManagerModule extends org.opendaylight.controller.config.yang.config.policy_manager.impl.AbstractPolicyManagerModule
                                 implements java.lang.AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(PolicyManagerModule.class);
	private PolicyValidator validator = null;
	private PolicyNotifier notifier = null;
	private PolicyLogger logger = null;
	private NetworkIntentCompilerTestFramework compiler = null;

	public PolicyManagerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public PolicyManagerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.policy_manager.impl.PolicyManagerModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        validator = new PolicyValidator(getDataBrokerDependency(),
                                        getRpcRegistryDependency(),
                                        getNotificationServiceDependency());
        notifier = new PolicyNotifier(getDataBrokerDependency(),
                                      getRpcRegistryDependency(),
                                      getNotificationServiceDependency());
        logger = new PolicyLogger(notifier);
        log.info("Created all except NIC");
        compiler = new NetworkIntentCompilerTestFramework(notifier,
                                                      getDataBrokerDependency());

        log.debug("Created PolicyValidator, PolicyNotifier, PolicyLogger, NetworkIntentCompiler");
        return this;
    }

    @Override
    public void close() throws Exception {
        validator.close();
        notifier.close();
        logger.close();
        compiler.close();
    }

}
