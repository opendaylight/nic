package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.bgp.renderer.rev170501;

import org.opendaylight.bgp.api.BGPRendererService;
import org.opendaylight.bgp.impl.BGPRouteServiceImpl;

public class BGPRendererProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.bgp.renderer.rev170501.AbstractBGPRendererProviderModule {
    public BGPRendererProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public BGPRendererProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.bgp.renderer.rev170501.BGPRendererProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final BGPRendererService rendererService = new BGPRouteServiceImpl(getDataBrokerDependency());
        rendererService.start();
        return rendererService;
    }
}
