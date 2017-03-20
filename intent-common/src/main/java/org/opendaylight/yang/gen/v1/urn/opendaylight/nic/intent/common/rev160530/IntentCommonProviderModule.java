package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.common.rev160530;

import org.opendaylight.nic.common.transaction.api.IntentCommonProviderService;
import org.opendaylight.nic.common.transaction.impl.IntentCommonProviderServiceImpl;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntentCommonProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.common.rev160530.AbstractIntentCommonProviderModule {
    public IntentCommonProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }
    private static final Logger LOG = LoggerFactory.getLogger(IntentCommonProviderModule.class);

    public IntentCommonProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.common.rev160530.IntentCommonProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("\nStarting Intent-Common module.");
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<?> ofServiceReference = context.getServiceReference(OFRendererFlowService.class);
        OFRendererFlowService ofRendererFlowService = (OFRendererFlowService) context.getService(ofServiceReference);

        final IntentCommonProviderService commonProvider = new IntentCommonProviderServiceImpl(getDataBrokerDependency(),
                ofRendererFlowService);
        commonProvider.start();

        return commonProvider;
    }

}
