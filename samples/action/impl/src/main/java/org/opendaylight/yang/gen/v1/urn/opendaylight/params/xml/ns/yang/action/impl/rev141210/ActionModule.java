package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.action.impl.rev141210;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.nic.action.impl.ActionProvider;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.action.impl.rev141210.AbstractActionModule {

    private static final Logger LOG = LoggerFactory.getLogger(ActionModule.class);
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public ActionModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public ActionModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.action.impl.rev141210.ActionModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("Action Renderer createInstance()");
        final ActionProvider provider = new ActionProvider(getDataBrokerDependency(), executor);
        getBrokerDependency().registerProvider(provider);
        DataBroker broker = getDataBrokerDependency();
        LogicalDatastoreType store = LogicalDatastoreType.CONFIGURATION;
        InstanceIdentifier<Intents> path = InstanceIdentifier.builder(Intents.class).build();
        DataChangeScope scope = DataChangeScope.SUBTREE;
        broker.registerDataChangeListener(store, path, provider, scope);
        return provider;
    }
}
