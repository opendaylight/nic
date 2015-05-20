package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.gbp.renderer.impl.rev150511;

import org.opendaylight.nic.gbp.renderer.impl.GBPRenderer;
import org.opendaylight.nic.gbp.renderer.impl.GBPRendererDataChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GBPRendererModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.gbp.renderer.impl.rev150511.AbstractGBPRendererModule {

    private GBPRendererDataChangeListener gBPRendererDataChangeListener;
    private static final Logger LOG = LoggerFactory.getLogger(GBPRendererModule.class);

    public GBPRendererModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public GBPRendererModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.gbp.renderer.impl.rev150511.GBPRendererModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final GBPRenderer renderer = new GBPRenderer(getDataBrokerDependency());
        gBPRendererDataChangeListener = new GBPRendererDataChangeListener(getDataBrokerDependency());
        final class CloseResources implements AutoCloseable {
            @Override
            public void close() throws Exception {
                if (renderer != null) {
                    renderer.close();
                }
                if (gBPRendererDataChangeListener != null) {
                    gBPRendererDataChangeListener.close();
                }
                LOG.info("Avaya (instance {}) torn down.", this);
            }
        }
        return renderer;
    }

}
