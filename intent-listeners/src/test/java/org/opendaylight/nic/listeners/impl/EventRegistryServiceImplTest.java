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
import org.opendaylight.nic.listeners.api.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import java.util.Dictionary;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

@PrepareForTest({ EventRegistryService.class,FrameworkUtil.class,
                    BundleContext.class, Bundle.class})
@RunWith(PowerMockRunner.class)
/**
 * JUnit test for {@link EventRegistryServiceImpl}.
 */
public class EventRegistryServiceImplTest {

    private ServiceRegistration<EventRegistryService> mockServiceReference;
    /**
     * Stubbed instance of EventRegistryServiceImpl to perform unit testing.
     */
    private EventRegistryServiceImpl mockRegistryServiceImpl;

    @Before
    public void setup() throws Exception {
        /**
         * Create required mock objects and define mocking functionality
         * for mock objects.
         */
        Bundle mockBundle = mock(Bundle.class);
        BundleContext mockBundleContext = mock(BundleContext.class);
        mockServiceReference = mock(ServiceRegistration.class);
        PowerMockito.mockStatic(FrameworkUtil.class);
        when(FrameworkUtil.getBundle(EventRegistryServiceImpl.class)).thenReturn(mockBundle);
        when(mockBundle.getBundleContext()).thenReturn(mockBundleContext);
        when(mockBundleContext.registerService(eq(EventRegistryService.class),
                any(EventRegistryService.class), isNull(Dictionary.class))).
                thenReturn(mockServiceReference);
        mockRegistryServiceImpl = spy(new EventRegistryServiceImpl());
    }

    /**
     * Test case for {@link EventRegistryService#registerEventListener(IEventService, IEventListener)}
     */
    @Test
    public void registerEventListenerWithServiceTest() {
        /**
         * Verify that listener is registered with the corresponding service using the Map implementation
         */
        IEventService mockService = mock(IEventService.class);
        IEventListener mockListener = mock(IEventListener.class);
        mockRegistryServiceImpl.registerEventListener(mockService, mockListener);
        Map<IEventService, Set<IEventListener<?>>> eventRegistry = mockRegistryServiceImpl.getEventRegistry();
        assertNotNull(eventRegistry);
        assertTrue(eventRegistry.size() == 1);
    }

    /**
     * Test case for {@link EventRegistryService#registerEventListener(EventType, IEventListener)}
     */
    @Test
    public void registerEventListenerWithTypesTest() {
        IEventListener mockListener = mock(IEventListener.class);
        IntentNotificationSupplierImpl mockIntentNotifSubscriber = mock (IntentNotificationSupplierImpl.class);
        /**
         * Set a supplier for a certain event type and check if
         * a listener can be registered on it
         */
        mockRegistryServiceImpl.setEventTypeService(mockIntentNotifSubscriber, EventType.INTENT_ADDED);
        mockRegistryServiceImpl.registerEventListener(EventType.INTENT_ADDED, mockListener);
        Map<IEventService, Set<IEventListener<?>>> eventRegistry = mockRegistryServiceImpl.getEventRegistry();
        assertNotNull(eventRegistry);
        assertTrue(eventRegistry.size() == 1);
    }

    /**
     * Test case for {@link EventRegistryService#unregisterEventListener(IEventService, IEventListener)}
     */
    @Test
    public void unregisterEventListenerTest() {
        IEventService mockService = mock(IEventService.class);
        IEventListener mockListener = mock(IEventListener.class);
        Map<IEventService, Set<IEventListener<?>>> eventRegistry = mockRegistryServiceImpl.getEventRegistry();
        mockRegistryServiceImpl.unregisterEventListener(mockService, mockListener);
        assertNotNull(eventRegistry);
        /**
         * Verify that event registry checks to see if service exists
         * There is no registered IEventService
         */
        assertFalse(eventRegistry.containsKey(mockService));
        /**
         * Register a service before deleting to verify if delete gets called
         */
        mockRegistryServiceImpl.registerEventListener(mockService, mockListener);
        assertTrue(eventRegistry.size() == 1);
        mockRegistryServiceImpl.unregisterEventListener(mockService, mockListener);
        assertTrue(eventRegistry.size() == 0);
    }

    /**
     * Test case for {@link EventRegistryService#setEventTypeService(IEventService, EventType...)}
     */
    @Test
    public void setEventTypeServiceTest() {
        IEventService mockService = mock(IEventService.class);
        mockRegistryServiceImpl.setEventTypeService(mockService, EventType.INTENT_ADDED,
                EventType.INTENT_REMOVED, EventType.INTENT_UPDATE);
        /**
         * Verify that the service is registered with the corresponding event types it publishes
         */
        Map<EventType, IEventService> typeRegistry = mockRegistryServiceImpl.getTypeRegistry();
        assertTrue(typeRegistry.size() == 3);
    }
}
