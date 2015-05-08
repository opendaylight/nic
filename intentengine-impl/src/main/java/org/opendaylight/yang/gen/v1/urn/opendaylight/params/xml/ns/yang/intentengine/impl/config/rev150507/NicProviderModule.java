package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.intentengine.impl.config.rev150507;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.intentengine.NicProvider;

public class NicProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.intentengine.impl.config.rev150507.AbstractNicProviderModule {

    public NicProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public NicProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver,
                             org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.intentengine.impl.config.rev150507.NicProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        DataBroker broker = getDataBrokerDependency();
        NicProvider provider = new NicProvider();
        provider.setDataBroker(broker);
        return provider;
    }

}
