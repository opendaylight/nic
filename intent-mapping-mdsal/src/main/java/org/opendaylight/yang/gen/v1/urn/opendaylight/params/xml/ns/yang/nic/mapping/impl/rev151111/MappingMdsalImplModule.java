package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nic.mapping.impl.rev151111;

import org.opendaylight.nic.mapping.mdsal.impl.MappingMdsalProvider;

public class MappingMdsalImplModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nic.mapping.impl.rev151111.AbstractMappingMdsalImplModule {
    public MappingMdsalImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public MappingMdsalImplModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nic.mapping.impl.rev151111.MappingMdsalImplModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final MappingMdsalProvider provider = new MappingMdsalProvider();
        getBrokerDependency().registerProvider(provider);

        return provider;
    }

}
