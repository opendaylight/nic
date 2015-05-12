/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;

public class NicProviderTest {

    @Test
    public void testOnSessionInitiated() throws Exception {
        try (NicProvider provider = new NicProvider()) {
            ProviderContext context = mock(BindingAwareBroker.ProviderContext.class);
            when(context.getSALService(DataBroker.class)).thenReturn(mock(DataBroker.class));
            DataBroker broker = mock(DataBroker.class);
            provider.setDataBroker(broker);

            NicProvider spy = Mockito.spy(provider);
            Mockito.doNothing().when(spy).initIntentsOperational();
            Mockito.doNothing().when(spy).initIntentsConfiguration();
            Mockito.doNothing().when(spy).setDataBroker(broker);

            // ensure no exceptions
            // currently this method is empty
            spy.onSessionInitiated(context);
        }
    }

    @Test
    public void testClose() throws Exception {
        try (NicProvider provider = new NicProvider()) {

        }
    }
}