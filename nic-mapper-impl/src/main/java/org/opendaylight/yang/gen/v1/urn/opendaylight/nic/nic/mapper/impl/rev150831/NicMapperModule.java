package org.opendaylight.yang.gen.v1.urn.opendaylight.nic.nic.mapper.impl.rev150831;

import org.opendaylight.nic.mapper.impl.NicMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NicMapperModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.nic.nic.mapper.impl.rev150831.AbstractNicMapperModule {
	private static final Logger LOG = LoggerFactory.getLogger(NicMapper.class);
	public NicMapperModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public NicMapperModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.nic.nic.mapper.impl.rev150831.NicMapperModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
    	 LOG.info("Creating a new NicMapper instance");
         final NicMapper mapper = new NicMapper();
         mapper.init();
         return mapper;
    }

}
