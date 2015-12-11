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
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.listeners.api.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({ Bundle.class, DataBroker.class, IntentNotificationSupplierImpl.class,
        FrameworkUtil.class, BundleContext.class})
@RunWith(PowerMockRunner.class)
/**
 * JUnit test for {@link IntentNotificationSupplierImpl}.
 */
public class IntentNotificationSupplierImplTest {

    /**
     * Mock instance of DataBroker to perform unit testing.
     */
    private DataBroker mockDataBroker;
    /**
     * Mock instance of EventRegistryService to perform unit testing.
     */
    private EventRegistryService mockRegistryService;
    /**
     * Stubbed instance of IntentNotificationSupplierImpl to perform unit testing.
     */
    private IntentNotificationSupplierImpl mockIntentSupplier;
    /**
     * Mock instance of Intent to perform unit testing.
     */
    private Intent mockIntent;
    /**
     * Mock instance of InstanceIdentifier<Intent> to perform unit testing.
     */
    private InstanceIdentifier<Intent> mockInstanceIdentifier;

    @Before
    public void setup() {
        /**
         * Create required mock objects and define mocking functionality
         * for mock objects.
         */
        mockDataBroker = mock(DataBroker.class);
        mockRegistryService = mock(EventRegistryService.class);
        mockIntent = mock(Intent.class);
        mockInstanceIdentifier= mock(InstanceIdentifier.class);
        Bundle mockBundle = mock(Bundle.class);
        BundleContext mockBundleContext = mock(BundleContext.class);
        ServiceReference<EventRegistryService> mockServiceReference = mock(ServiceReference.class);
        PowerMockito.mockStatic(FrameworkUtil.class);
        when(FrameworkUtil.getBundle(IntentNotificationSupplierImpl.class)).thenReturn(mockBundle);
        when(mockBundle.getBundleContext()).thenReturn(mockBundleContext);
        when(mockBundleContext.getServiceReference(EventRegistryService.class)).thenReturn(mockServiceReference);
        when(mockBundleContext.getService(mockServiceReference)).thenReturn(mockRegistryService);
        mockIntentSupplier = new IntentNotificationSupplierImpl(mockDataBroker);
        verify(mockRegistryService).setEventTypeService(mockIntentSupplier,
                EventType.INTENT_ADDED, EventType.INTENT_REMOVED, EventType.INTENT_UPDATE);
    }

    /**
     * Test case for {@link IntentNotificationSupplierImpl#createNotification(Intent, InstanceIdentifier)}
     */
    @Test
    public void createNotificationTest() {
        IntentAdded mockIntentAdded = mockIntentSupplier.createNotification(mockIntent, mockInstanceIdentifier);
        assertNotNull(mockIntentAdded);
        assertEquals(mockIntentAdded.getIntent(), mockIntent);
    }

    /**
     * Test case for {@link IntentNotificationSupplierImpl#deleteNotification(Intent, InstanceIdentifier)}
     */
    @Test
    public void deleteNotificationTest() {
        IntentRemoved mockIntentRemoved = mockIntentSupplier.deleteNotification(mockIntent, mockInstanceIdentifier);
        assertNotNull(mockIntentRemoved);
        assertEquals(mockIntentRemoved.getIntent(), mockIntent);
    }

    /**
     * Test case for {@link IntentNotificationSupplierImpl#updateNotification(Intent, InstanceIdentifier)}
     */
    @Test
    public void updateNotificationTest() {
        IntentUpdated mockIntentUpdated = mockIntentSupplier.updateNotification(mockIntent, mockInstanceIdentifier);
        assertNotNull(mockIntentUpdated);
        assertEquals(mockIntentUpdated.getIntent(), mockIntent);
    }

    /**
     * Test case for {@link IntentNotificationSupplierImpl#addEventListener(IEventListener)}
     */
    @Test
    public void addEventListenerTest() {
        IEventListener<?> mockListener = mock(IEventListener.class);
        mockIntentSupplier.addEventListener(mockListener);
        verify(mockRegistryService).registerEventListener(mockIntentSupplier, mockListener);
    }

    /**
     * Test case for {@link IntentNotificationSupplierImpl#removeEventListener(IEventListener)}
     */
    @Test
    public void removeEventListenerTest() {
        IEventListener<?> mockListener = mock(IEventListener.class);
        mockIntentSupplier.removeEventListener(mockListener);
        verify(mockRegistryService).unregisterEventListener(mockIntentSupplier, mockListener);
    }
}
