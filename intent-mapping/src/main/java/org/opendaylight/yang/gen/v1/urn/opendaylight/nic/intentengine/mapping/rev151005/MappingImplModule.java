package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intentengine.mapping.rev151005;

import org.opendaylight.nic.mapping.impl.HazelcastMappingServiceImpl;

public class MappingImplModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intentengine.mapping.rev151005.AbstractMappingImplModule {
    public MappingImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public MappingImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intentengine.mapping.rev151005.MappingImplModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final HazelcastMappingServiceImpl provider = new HazelcastMappingServiceImpl();
        provider.init();
        return provider;
    }

}
