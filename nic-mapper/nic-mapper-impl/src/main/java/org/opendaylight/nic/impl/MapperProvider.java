/*
 * Copyright (c) 2015 HP Inc and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.impl;

import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.mapper.rev150105.MapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapperProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(MapperProvider.class);
    private RpcRegistration<MapperService> mapperService;

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("MapperProvider Session Initiated");
        mapperService = session.addRpcImplementation(MapperService.class, new GetValueImpl());
    }

    @Override
    public void close() throws Exception {
        LOG.info("MapperProvider Closed");
        if(mapperService != null){
        	mapperService.close();
        }
    }

}
