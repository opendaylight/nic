package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.nemo.renderer.rev151001;

import org.opendaylight.nic.nemo.renderer.NEMORenderer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NEMORendererModule extends
        org.opendaylight.yang.gen.v1.urn.opendaylight.nic.nemo.renderer.rev151001.AbstractNEMORendererModule {

    private static final Logger LOG = LoggerFactory.getLogger(NEMORendererModule.class);

    public NEMORendererModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public NEMORendererModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
            org.opendaylight.controller.config.api.DependencyResolver dependencyResolver,
            org.opendaylight.yang.gen.v1.urn.opendaylight.nic.nemo.renderer.rev151001.NEMORendererModule oldModule,
            java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("Creating NEMO Renderer.");

        final NEMORenderer renderer = new NEMORenderer(getDataBrokerDependency());
        renderer.init();

        final BundleContext context = FrameworkUtil.getBundle(NEMORendererModule.class).getBundleContext();
        final ServiceRegistration<NEMORenderer> serviceRegistration = context.registerService(NEMORenderer.class,
                renderer, null);

        return new AutoCloseable() {

            @Override
            public void close() throws Exception {
                if (serviceRegistration != null) {
                    serviceRegistration.unregister();
                }

                if (renderer != null) {
                    renderer.close();
                }
            }
        };
    }
}
