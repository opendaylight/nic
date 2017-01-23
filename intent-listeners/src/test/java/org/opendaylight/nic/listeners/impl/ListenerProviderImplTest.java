/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.nic.listeners.api.EventRegistryService;
import org.opendaylight.nic.listeners.api.NotificationSupplierDefinition;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.mockito.Mockito.*;

@PrepareForTest({ OFRendererFlowService.class, DataBroker.class,
        NotificationService.class, FrameworkUtil.class })
@RunWith(PowerMockRunner.class)
/**
 * JUnit test for {@link NeutronIntegrationProviderImpl}.
 */
public class ListenerProviderImplTest {
    /**
     * Mock instance of DataBroker to perform unit testing.
     */
    @Mock
    private DataBroker mockDataBroker;

    /**
     * Mock instance of NotificationService to perform unit testing.
     */
    @Mock
    private NotificationService mockNotificationService;

    /**
     * Mock instance of OFRendererFlowService to perform unit testing.
     */
    @Mock
    private OFRendererFlowService mockFlowService;

    /**
     * Mock instance of OFRendererGraphService to perform unit testing.
     */
    @Mock
    private OFRendererGraphService mockGraphService;

    /**
     * Stubbed instance of ListenerProviderImpl to perform unit testing.
     */
    private ListenerProviderImpl provider;

    /**
     * create a mock object for the class Bundle.
     */
    @Mock
    private Bundle bundle;

    /**
     * create a mock Object for the class BundleContext.
     */
    @Mock
    private BundleContext context;

    @Mock
    private EventRegistryServiceImpl eventRegistryServiceImpl;

    @Mock
    private EventRegistryService eventRegistryService;

    /**
     * create a mock Object for the class ServiceRegistration.
     */
    @Mock
    private ServiceRegistration<EventRegistryService> nicEventServiceRegistration;

    @Mock
    private ServiceReference serviceReference;

    @Mock
    private EndpointDiscoveredNotificationSupplierImpl endpointDiscoveredNotificationSupplierImpl;

    @Mock
    private ListenerRegistration<EndpointDiscoveredNotificationSupplierImpl> notificationListenerRegistration;

    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(FrameworkUtil.class);
        when(FrameworkUtil.getBundle(EventRegistryServiceImpl.class))
                .thenReturn(bundle);
        when(bundle.getBundleContext()).thenReturn(context);
        when(context.registerService(EventRegistryService.class,
                eventRegistryServiceImpl, null))
                        .thenReturn(nicEventServiceRegistration);

        when(mockNotificationService.registerNotificationListener(
                endpointDiscoveredNotificationSupplierImpl))
                        .thenReturn(notificationListenerRegistration);
        /**
         * Create required mock objects and define mocking functionality for
         * mock objects.
         */
        provider = Mockito.spy(new ListenerProviderImpl(mockDataBroker,
                mockNotificationService, mockFlowService, mockGraphService));
    }

    /**
     * Test case for {@link ListenerProviderImpl#start()}
     */
    @Test
    public void testStart() throws Exception {
        when(FrameworkUtil.getBundle(NodeNotificationSupplierImpl.class))
                .thenReturn(bundle);
        when(FrameworkUtil
                .getBundle(NodeConnectorNotificationSupplierImpl.class))
                        .thenReturn(bundle);
        when(FrameworkUtil.getBundle(IntentNotificationSupplierImpl.class))
                .thenReturn(bundle);
        when(FrameworkUtil.getBundle(IntentNBINotificationSupplierImpl.class))
                .thenReturn(bundle);
        when(FrameworkUtil
                .getBundle(NeutronSecGroupNotificationSupplierImpl.class))
                        .thenReturn(bundle);
        when(FrameworkUtil
                .getBundle(NeutronSecRuleNotificationSupplierImpl.class))
                        .thenReturn(bundle);
        when(FrameworkUtil
                .getBundle(TopologyLinkNotificationSupplierImpl.class))
                        .thenReturn(bundle);
        when(FrameworkUtil
                .getBundle(EndpointDiscoveredNotificationSupplierImpl.class))
                        .thenReturn(bundle);

        when(context.getServiceReference(EventRegistryService.class))
                .thenReturn(serviceReference);
        when((EventRegistryService) context.getService(serviceReference))
                .thenReturn(eventRegistryService);
        provider.start();

        List<NotificationSupplierDefinition<?>> mockSupplierList = provider
                .getSupplierList();

        /**
         * Verify that the listeners are registered with the
         * EventRegistryService Need improvement here.
         */
        boolean result = false;

        for (NotificationSupplierDefinition<?> notificationSupplierDefinition : mockSupplierList) {
            result = false;

            if (notificationSupplierDefinition instanceof NodeNotificationSupplierImpl) {
                result = true;
            } else if (notificationSupplierDefinition instanceof NodeConnectorNotificationSupplierImpl) {
                result = true;
            } else if (notificationSupplierDefinition instanceof IntentNotificationSupplierImpl) {
                result = true;
            } else if (notificationSupplierDefinition instanceof IntentNBINotificationSupplierImpl) {
                result = true;
            } else if (notificationSupplierDefinition instanceof NeutronSecGroupNotificationSupplierImpl) {
                result = true;
            } else if (notificationSupplierDefinition instanceof NeutronSecRuleNotificationSupplierImpl) {
                result = true;
            } else if (notificationSupplierDefinition instanceof TopologyLinkNotificationSupplierImpl) {
                result = true;
            } else {
                break;
            }
        }

        Assert.assertTrue(result);
    }
}
