package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.mapping.rev151005;
import org.opendaylight.nic.mapping.hazelcast.impl.HazelcastMappingServiceImpl;

public class MappingHazelcastImplModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.mapping.rev151005.AbstractMappingHazelcastImplModule {
    public MappingHazelcastImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public MappingHazelcastImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.mapping.rev151005.MappingHazelcastImplModule oldModule, java.lang.AutoCloseable oldInstance) {
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
