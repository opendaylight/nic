package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.vtn.renderer.rev150508;

import org.opendaylight.nic.vtn.renderer.VTNRenderer;

public class VTNRendererModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.vtn.renderer.rev150508.AbstractVTNRendererModule {
    public VTNRendererModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public VTNRendererModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.vtn.renderer.rev150508.VTNRendererModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        VTNRenderer provider = new VTNRenderer();
        getBrokerDependency().registerProvider(provider);

        return provider;
    }

}
