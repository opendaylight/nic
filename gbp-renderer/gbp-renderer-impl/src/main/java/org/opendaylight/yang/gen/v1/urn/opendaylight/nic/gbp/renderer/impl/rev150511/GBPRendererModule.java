package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.gbp.renderer.impl.rev150511;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.gbp.renderer.impl.GBPRenderer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GBPRendererModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.gbp.renderer.impl.rev150511.AbstractGBPRendererModule {

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
        DataBroker broker = getDataBrokerDependency();

        LogicalDatastoreType store = LogicalDatastoreType.CONFIGURATION;
        InstanceIdentifier<Intents> path = InstanceIdentifier.builder(Intents.class).build();

        final GBPRenderer renderer = new GBPRenderer();
        DataChangeScope scope = DataChangeScope.SUBTREE;
        final ListenerRegistration<DataChangeListener> reg = broker.registerDataChangeListener(store, path, renderer, scope);

        final class CloseResources implements AutoCloseable {
            @Override
            public void close() throws Exception {
                if (renderer != null) {
                    renderer.close();
                }
                if (reg != null) {
                    reg.close();
                }
                LOG.info("GBG Renderer (instance {}) torn down.", this);
            }
        }

        AutoCloseable ret = new CloseResources();
        LOG.info("GBG Renderer: (instance {}) initialized.", ret);
        return ret;
    }

}
