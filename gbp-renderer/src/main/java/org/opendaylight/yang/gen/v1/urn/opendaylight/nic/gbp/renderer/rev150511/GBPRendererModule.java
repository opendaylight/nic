package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.gbp.renderer.rev150511;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.nic.gbp.renderer.GBPRenderer;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GBPRendererModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.gbp.renderer.rev150511.AbstractGBPRendererModule {
    public GBPRendererModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public GBPRendererModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.gbp.renderer.rev150511.GBPRendererModule oldModule, java.lang.AutoCloseable oldInstance) {
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

        GBPRenderer renderer = new GBPRenderer();
        DataChangeScope scope = DataChangeScope.SUBTREE;
        ListenerRegistration<DataChangeListener> reg = broker.registerDataChangeListener(store, path, renderer, scope);

        return renderer;
    }

}
