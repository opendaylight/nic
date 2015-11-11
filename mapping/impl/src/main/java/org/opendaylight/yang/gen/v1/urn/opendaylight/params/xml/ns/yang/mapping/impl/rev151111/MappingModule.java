package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mapping.impl.rev151111;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.mapping.impl.MappingProvider;

public class MappingModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mapping.impl.rev151111.AbstractMappingModule {
    public MappingModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public MappingModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mapping.impl.rev151111.MappingModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        // TODO:implement
        //throw new java.lang.UnsupportedOperationException();
        final MappingProvider provider = new MappingProvider();

        //TO-DO: get data and rpc registry dependency

        final class AutoCloseableMapping implements AutoCloseable {

            @Override
            public void close() throws Exception {
                provider.close();
            }
        }
        return new AutoCloseableMapping();
    }

}
