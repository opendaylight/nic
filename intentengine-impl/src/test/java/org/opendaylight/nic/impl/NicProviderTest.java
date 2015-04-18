/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.impl;

import org.junit.Test;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intentapi.rev150417.IntentapiService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class NicProviderTest {
    @Test
    public void testOnSessionInitiated() {
        NicProvider provider = new NicProvider();

        // ensure no exceptions
        // currently this method is empty
        provider.onSessionInitiated(mock(BindingAwareBroker.ProviderContext.class));
    }

    @Test
    public void testClose() throws Exception {
        NicProvider provider = new NicProvider();

        // ensure no exceptions
        
        RpcRegistration<IntentapiService> serviceMock = mock(RpcRegistration.class);
        provider.intentapiService = serviceMock;
        
        //expectation
        serviceMock.close();
                
        provider.close();
        
        //verification
        verify(serviceMock, times(1)).close();
    }
}
