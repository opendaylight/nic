package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.listeners.rev150916;

import org.opendaylight.nic.listeners.impl.ListenerProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListenerProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.listeners.rev150916.AbstractListenerProviderModule {
    private static final Logger LOG = LoggerFactory.getLogger(ListenerProviderModule.class);

    public ListenerProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public ListenerProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.listeners.rev150916.ListenerProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final ListenerProviderImpl provider = new ListenerProviderImpl(getDataBrokerDependency(), getNotificationAdapterDependency());
        provider.start();
        LOG.info("NIC Listeners started successfully.");

        return new AutoCloseable() {

            @Override
            public void close() throws Exception {
                try {
                    provider.close();
                } catch (final Exception e) {
                    LOG.error("Unexpected error while stopping ListenerProviderModule", e);
                }
                LOG.info("ListenerProviderModule stopped.");
            }
        };
    }
}
