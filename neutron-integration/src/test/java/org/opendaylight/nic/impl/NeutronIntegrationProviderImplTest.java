/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.nic.listeners.api.EventRegistryService;
import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.nic.listeners.impl.EventRegistryServiceImpl;
import org.opendaylight.nic.neutron.integration.impl.NeutronIntegrationProviderImpl;
import org.opendaylight.nic.neutron.integration.impl.SecGroupNotificationSubscriberImpl;
import org.opendaylight.nic.neutron.integration.impl.SecRuleNotificationSubscriberImpl;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.*;

@PrepareForTest({ EventRegistryService.class })
@RunWith(PowerMockRunner.class)
/**
 * JUnit test for {@link NeutronIntegrationProviderImpl}.
 */
public class NeutronIntegrationProviderImplTest {
    /**
     * Mock instance of DataBroker to perform unit testing.
     */
    private DataBroker mockDataBroker;
    /**
     * Mock instance of EventRegistryService to perform unit testing.
     */
    private EventRegistryService mockServiceRegistry;
    /**
     * Mock instance of NicConsoleProvider to perform unit testing.
     */
    private NicConsoleProvider mockConsoleProvider;

    private NeutronIntegrationProviderImpl provider = null;


    @Before
    public void setup() throws Exception {
        /**
         * Create required mock objects and define mocking functionality
         * for mock objects.
         */
        mockServiceRegistry = mock(EventRegistryService.class);
        mockDataBroker = mock(DataBroker.class);
        mockConsoleProvider = mock(NicConsoleProvider.class);
        provider = spy(new NeutronIntegrationProviderImpl(mockDataBroker, mockServiceRegistry, mockConsoleProvider));
    }

    /**
     * Test case for {@link NeutronIntegrationProviderImpl#start()} ()}
     */
    @Test
    public void testStart() {
        /**
         * Verify start() should register Security group and Security rules
         * listeners with the EventRegistryService.
         */
        provider.start();
        verify(mockServiceRegistry,times(1)).
                registerEventListener(eq(EventType.SECURITY_GROUP_ADDED), Mockito.any(SecGroupNotificationSubscriberImpl.class));
        verify(mockServiceRegistry,times(1)).
                registerEventListener(eq(EventType.SECURITY_RULE_ADDED), Mockito.any(SecRuleNotificationSubscriberImpl.class));
    }

}
