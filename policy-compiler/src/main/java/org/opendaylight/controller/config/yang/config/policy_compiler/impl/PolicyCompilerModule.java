package org.opendaylight.controller.config.yang.config.policy_compiler.impl;

import org.opendaylight.controller.config.spi.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolicyCompilerModule extends org.opendaylight.controller.config.yang.config.policy_compiler.impl.AbstractPolicyCompilerModule
                                  implements java.lang.AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(PolicyCompilerModule.class);

    public PolicyCompilerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public PolicyCompilerModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.controller.config.yang.config.policy_compiler.impl.PolicyCompilerModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        log.info("Created PolicyCompilerModule instance");
        return this;
    }

    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub
    }

}
