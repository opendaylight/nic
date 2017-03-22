package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.of.renderer.rev150819;

import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.nic.of.renderer.impl.OFRendererFlowManagerProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.idmanager.rev160406.IdManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OFRendererProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.of.renderer.rev150819.AbstractOFRendererProviderModule {
    private static final Logger LOG = LoggerFactory.getLogger(OFRendererProviderModule.class);

    public OFRendererProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public OFRendererProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.of.renderer.rev150819.OFRendererProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.info("Creating Open flow renderer");

        RpcProviderRegistry rpcProviderRegistry = getRpcRegistryDependency();
        IdManagerService idManagerService = rpcProviderRegistry.getRpcService(IdManagerService.class);

        final OFRendererFlowManagerProvider provider =
                new OFRendererFlowManagerProvider(getDataBrokerDependency(),
                        getPipelineManagerDependency(),
                        getIntentMappingInterfaceDependency(),
                        getNotificationServiceDependency(),
                        idManagerService);
        provider.init();
        return () -> provider.close();
    }

}
