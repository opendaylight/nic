package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.listeners.rev150916;

import org.opendaylight.nic.listeners.impl.ListenerProviderImpl;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
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
        // Retrieve reference for OFRenderer service
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<?> ofServiceReference = context.
                getServiceReference(OFRendererFlowService.class);
        OFRendererFlowService flowService = (OFRendererFlowService) context.
                getService(ofServiceReference);
        ServiceReference<?> graphServiceReference = context.
                getServiceReference(OFRendererGraphService.class);
        OFRendererGraphService graphService = (OFRendererGraphService) context
                .getService(graphServiceReference);

        final ListenerProviderImpl provider = new ListenerProviderImpl(getDataBrokerDependency(),
                                                                       getNotificationAdapterDependency(),
                                                                       flowService,
                                                                       graphService);
        provider.start();
        LOG.info("NIC Listeners started successfully.");

        return provider;
    }
}
