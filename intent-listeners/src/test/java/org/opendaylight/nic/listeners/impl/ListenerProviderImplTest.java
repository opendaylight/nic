/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.nic.common.transaction.api.IntentCommonService;
import org.opendaylight.nic.common.transaction.impl.IntentCommonServiceManager;
import org.opendaylight.nic.engine.IntentStateMachineExecutorService;
import org.opendaylight.nic.listeners.api.NotificationSupplierDefinition;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

@PrepareForTest({ OFRendererFlowService.class, DataBroker.class,
                    NotificationService.class, ListenerProviderImpl.class})
@RunWith(PowerMockRunner.class)
/**
 * JUnit test for {@link NeutronIntegrationProviderImpl}.
 */
public class ListenerProviderImplTest {
    /**
     * Mock instance of DataBroker to perform unit testing.
     */
    private DataBroker mockDataBroker;
    /**
     * Mock instance of NotificationService to perform unit testing.
     */
    private NotificationService mockNotificationService;
    /**
     * Mock instance of OFRendererFlowService to perform unit testing.
     */
    private OFRendererFlowService mockFlowService;
    /**
     * Mock instance of OFRendererGraphService to perform unit testing.
     */
    private OFRendererGraphService mockGraphService;
    /**
     * Stubbed instance of ListenerProviderImpl to perform unit testing.
     */
    private ListenerProviderImpl provider;
    /**
     * Stubbed instance of IntentCommonService to perform unit testing.
     */
    private IntentCommonService mockIntentCommonService;
    /**
     * Stubbed instance of IntentStateMachineExecutorService to perform unit testing.
     */
    private IntentStateMachineExecutorService mockStateMachineExecutorService;


    @Before
    public void setup() throws Exception {
        /**
         * Create required mock objects and define mocking functionality
         * for mock objects.
         */
        mockDataBroker = mock(DataBroker.class);
        mockNotificationService = mock(NotificationService.class);
        mockFlowService = mock(OFRendererFlowService.class);
        mockGraphService = mock(OFRendererGraphService.class);
        mockIntentCommonService = mock(IntentCommonService.class);
        mockStateMachineExecutorService = mock(IntentStateMachineExecutorService.class);
        provider = PowerMockito.spy(new ListenerProviderImpl(mockDataBroker,
                mockNotificationService,
                mockFlowService,
                mockGraphService,
                mockIntentCommonService,
                mockStateMachineExecutorService));
    }



    /**
     * Test case for {@link ListenerProviderImpl#start()}
     */
    @Test
    public void testStart() throws Exception {
        EventRegistryServiceImpl mockRegistryServiceImpl = mock(EventRegistryServiceImpl.class);
        PowerMockito.whenNew(EventRegistryServiceImpl.class).withNoArguments().thenReturn(mockRegistryServiceImpl);
        ArrayList<NotificationSupplierDefinition<?>> mockSupplierList = mock(ArrayList.class);
        PowerMockito.whenNew(ArrayList.class).withAnyArguments().thenReturn(mockSupplierList);

        NodeNotificationSupplierImpl mockNodeSupp =
                mock(NodeNotificationSupplierImpl.class);
        NodeConnectorNotificationSupplierImpl mockConnectorSupp =
                mock(NodeConnectorNotificationSupplierImpl.class);
        IntentNotificationSupplierImpl mockIntentSupp =
                mock(IntentNotificationSupplierImpl.class);
        IntentNBINotificationSupplierImpl mockIntentNBISupp =
                mock(IntentNBINotificationSupplierImpl.class);
        EndpointDiscoveredNotificationSupplierImpl mockEndpointResolver =
                mock(EndpointDiscoveredNotificationSupplierImpl.class);
        TopologyLinkNotificationSupplierImpl mockLinkSupp =
                mock(TopologyLinkNotificationSupplierImpl.class);
        IntentCommonServiceManager intentCommonServiceMock =
                mock(IntentCommonServiceManager.class);
        IntentLimiterNotificationSupplierImpl mockIntentLimiterSupp =
                mock(IntentLimiterNotificationSupplierImpl.class);
        TransactionStateNotificationSuplierImpl mockTransactionStateSupp =
                mock(TransactionStateNotificationSuplierImpl.class);

        PowerMockito.whenNew(NodeNotificationSupplierImpl.class).
                withAnyArguments().thenReturn(mockNodeSupp);
        PowerMockito.whenNew(NodeConnectorNotificationSupplierImpl.class).
                withAnyArguments().thenReturn(mockConnectorSupp);
        PowerMockito.whenNew(IntentNotificationSupplierImpl.class).
                withAnyArguments().thenReturn(mockIntentSupp);
        PowerMockito.whenNew(IntentNBINotificationSupplierImpl.class).
                withAnyArguments().thenReturn(mockIntentNBISupp);
        PowerMockito.whenNew(EndpointDiscoveredNotificationSupplierImpl.class).
                withAnyArguments().thenReturn(mockEndpointResolver);
        PowerMockito.whenNew(TopologyLinkNotificationSupplierImpl.class).
                withAnyArguments().thenReturn(mockLinkSupp);
        PowerMockito.whenNew(IntentCommonServiceManager.class).
                withAnyArguments().thenReturn(intentCommonServiceMock);
        PowerMockito.whenNew(IntentLimiterNotificationSupplierImpl.class).
                withAnyArguments().thenReturn(mockIntentLimiterSupp);
        PowerMockito.whenNew(TransactionStateNotificationSuplierImpl.class).
                withAnyArguments().thenReturn(mockTransactionStateSupp);

        provider.start();

        /**
         * Verify that the listeners are registered with the EventRegistryService
         */
        verify(mockRegistryServiceImpl).registerEventListener(
                eq(mockNodeSupp), Mockito.any(NodeNotificationSubscriberImpl.class));
        verify(mockRegistryServiceImpl).registerEventListener(
                eq(mockIntentSupp), Mockito.any(IntentNotificationSubscriberImpl.class));
        verify(mockRegistryServiceImpl).registerEventListener(
                eq(mockEndpointResolver), Mockito.any(EndpointDiscoveryNotificationSubscriberImpl.class));
        verify(mockRegistryServiceImpl).registerEventListener(
                eq(mockLinkSupp), Mockito.any(TopologyLinkNotificationSubscriberImpl.class));
        verify(mockSupplierList,times(9)).add(Mockito.any(NotificationSupplierDefinition.class));

    }
}
