/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.impl;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;

public class NicProviderTest {

    // @Test
    public void testOnSessionInitiated() throws Exception {
        DataBroker broker = mock(DataBroker.class);
        try (NicProvider provider = new NicProvider(broker)) {

            NicProvider spy = Mockito.spy(provider);
            Mockito.doNothing().when(spy).initIntentsOperational();
            Mockito.doNothing().when(spy).initIntentsConfiguration();

            // ensure no exceptions
            // currently this method is empty
            spy.init();
        }
    }

    @Test
    public void testClose() throws Exception {
        DataBroker broker = mock(DataBroker.class);
        try (NicProvider provider = new NicProvider(broker)) {

        }
    }
}
