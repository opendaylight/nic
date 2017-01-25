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
import org.opendaylight.controller.liblldp.PacketException;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.nic.listeners.api.EndpointDiscovered;
import org.opendaylight.nic.listeners.api.EventRegistryService;
import org.opendaylight.nic.utils.EventType;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.utils.Arp;
import org.opendaylight.nic.listeners.utils.ArpOperation;
import org.opendaylight.nic.listeners.utils.ArpResolverUtils;
import org.opendaylight.nic.listeners.utils.ArpUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

@PrepareForTest({ FrameworkUtil.class, ArpUtils.class, BundleContext.class,
        ArpResolverUtils.class})
@RunWith(PowerMockRunner.class)
/**
 * JUnit test for {@link EndpointDiscoveredNotificationSupplierImpl}.
 */
public class EndpointDiscoveredNotificationSupplierImplTest {
    /**
     * Stubbed instance of EndpointDiscoveredNotificationSupplierImpl to perform unit testing.
     */
    private EndpointDiscoveredNotificationSupplierImpl mockSupplier;
    /**
     * Mock instance of NotificationService to perform unit testing.
     */
    private NotificationService mockNotificationService;
    /**
     * Stubbed instance of EventRegistryService to perform unit testing.
     */
    private EventRegistryService mockRegistryService;

    @Before
    public void setup() {
        mockNotificationService = mock(NotificationService.class);
        mockRegistryService = mock(EventRegistryService.class);
        Bundle mockBundle = mock(Bundle.class);
        BundleContext mockBundleContext = mock(BundleContext.class);
        ServiceReference<EventRegistryService> mockServiceReference = mock(ServiceReference.class);
        PowerMockito.mockStatic(FrameworkUtil.class);
        when(FrameworkUtil.getBundle(EndpointDiscoveredNotificationSupplierImpl.class)).thenReturn(mockBundle);
        when(mockBundle.getBundleContext()).thenReturn(mockBundleContext);
        when(mockBundleContext.getServiceReference(EventRegistryService.class)).thenReturn(mockServiceReference);
        when(mockBundleContext.getService(mockServiceReference)).thenReturn(mockRegistryService);
        mockSupplier = new EndpointDiscoveredNotificationSupplierImpl(mockNotificationService);
    }

    /**
     * Test case for {@link EndpointDiscoveredNotificationSupplierImpl#onPacketReceived(PacketReceived)}
     * Verify if Arp packet returns a PacketException.
     */
    @Test
    public void onPacketReceivedArpExceptionTest() throws Exception {
        PacketReceived mockPacket = mock(PacketReceived.class);
        PowerMockito.mockStatic(ArpResolverUtils.class);

        when(ArpResolverUtils.getArpFrom(mockPacket)).thenThrow(new PacketException("Packet is not ARP: "));

        IEventListener mockListener =
                mock(EndpointDiscoveryNotificationSubscriberImpl.class);
        Set<IEventListener<?>> eventListenerSet = new HashSet<>();
        eventListenerSet.add(mockListener);
        when(mockRegistryService.getEventListeners(EventType.ENDPOINT_DISCOVERED));

        /**
         * Verify that the appropriate listener handlers are called
         */
        mockSupplier.onPacketReceived(mockPacket);
        verify(mockListener, Mockito.times(0)).handleEvent(Mockito.any(EndpointDiscovered.class));
    }

    /**
     * Test case for {@link EndpointDiscoveredNotificationSupplierImpl#onPacketReceived(PacketReceived)}
     * Verify if Arp packet gets processed to retrieve the right fields and calls the right listeners
     */
    @Test
    public void onPacketReceivedRequestTest() throws Exception {
        PacketReceived mockPacket = mock(PacketReceived.class);
        PowerMockito.mockStatic(ArpResolverUtils.class);
        PowerMockito.mockStatic(ArpUtils.class);

        // Create Arp object with test fields
        String macAddress = "AA:BB:CC:DD:EE:FF";
        String[] macAddressParts = macAddress.split(":");

        // Convert hex string to byte values
        byte[] macAddressBytes = new byte[6];
        for(int i=0; i<6; i++){
            Integer hex = Integer.parseInt(macAddressParts[i], 16);
            macAddressBytes[i] = hex.byteValue();
        }

        String ipAddress = "192.168.1.1";
        String[] ipAddressParts = ipAddress.split("\\.");

        // Convert int string to byte values
        byte[] ipAddressBytes = new byte[4];
        for(int i=0; i<4; i++){
            Integer integer = Integer.parseInt(ipAddressParts[i]);
            ipAddressBytes[i] = integer.byteValue();
        }

        Arp mockArp = new Arp();
        mockArp.setSenderHardwareAddress(macAddressBytes);
        mockArp.setSenderProtocolAddress(ipAddressBytes);
        mockArp.setOperation(ArpOperation.REQUEST.intValue());
        when(ArpResolverUtils.getArpFrom(mockPacket)).thenReturn(mockArp);

        IEventListener mockListener =
                mock(EndpointDiscoveryNotificationSubscriberImpl.class);
        Set<IEventListener<?>> eventListenerSet = new HashSet<>();
        eventListenerSet.add(mockListener);
        when(mockRegistryService.getEventListeners(EventType.ENDPOINT_DISCOVERED))
                .thenReturn(eventListenerSet);

        /**
         * Verify that the appropriate listener handlers are called
         */
        mockSupplier.onPacketReceived(mockPacket);
        verify(mockListener).handleEvent(Mockito.any(EndpointDiscovered.class));
    }

    /**
     * Test case for {@link EndpointDiscoveredNotificationSupplierImpl#onPacketReceived(PacketReceived)}
     * Verify if Arp packet gets processed to retrieve the right fields and calls the right listeners
     */
    @Test
    public void onPacketReceivedReplyTest() throws Exception {
        PacketReceived mockPacket = mock(PacketReceived.class);
        PowerMockito.mockStatic(ArpResolverUtils.class);
        PowerMockito.mockStatic(ArpUtils.class);

        // Create Arp object with test fields
        String macAddressSender = "AA:BB:CC:DD:EE:FF";
        String[] macAddressPartsSender = macAddressSender.split(":");

        // Create Arp object with test fields
        String macAddressTarget = "AA:BB:CC:DD:EE:FE";
        String[] macAddressPartsTarget = macAddressTarget.split(":");

        // Convert hex string to byte values
        byte[] macAddressBytesSender = new byte[6];
        byte[] macAddressBytesTarget = new byte[6];
        for(int i=0; i<6; i++){
            Integer hex = Integer.parseInt(macAddressPartsSender[i], 16);
            macAddressBytesSender[i] = hex.byteValue();
            hex = Integer.parseInt(macAddressPartsTarget[i], 16);
            macAddressBytesTarget[i] = hex.byteValue();
        }

        String ipAddressSender = "192.168.1.1";
        String[] ipAddressPartsSender = ipAddressSender.split("\\.");

        String ipAddressTarget = "192.168.1.2";
        String[] ipAddressPartsTarget = ipAddressTarget.split("\\.");

        // Convert int string to byte values
        byte[] ipAddressBytesSender = new byte[4];
        byte[] ipAddressBytesTarget = new byte[4];
        for(int i=0; i<4; i++){
            Integer integer = Integer.parseInt(ipAddressPartsSender[i]);
            ipAddressBytesSender[i] = integer.byteValue();
            integer = Integer.parseInt(ipAddressPartsTarget[i]);
            ipAddressBytesTarget[i] = integer.byteValue();
        }

        Arp mockArp = new Arp();
        mockArp.setTargetHardwareAddress(macAddressBytesTarget);
        mockArp.setTargetProtocolAddress(ipAddressBytesTarget);
        mockArp.setSenderHardwareAddress(macAddressBytesSender);
        mockArp.setSenderProtocolAddress(ipAddressBytesSender);
        mockArp.setOperation(ArpOperation.REPLY.intValue());
        when(ArpResolverUtils.getArpFrom(mockPacket)).thenReturn(mockArp);

        IEventListener mockListener =
                mock(EndpointDiscoveryNotificationSubscriberImpl.class);
        Set<IEventListener<?>> eventListenerSet = new HashSet<>();
        eventListenerSet.add(mockListener);
        when(mockRegistryService.getEventListeners(EventType.ENDPOINT_DISCOVERED))
                .thenReturn(eventListenerSet);

        /**
         * Verify that the appropriate listener handlers are called
         */
        mockSupplier.onPacketReceived(mockPacket);
        verify(mockListener, Mockito.times(2)).handleEvent(Mockito.any(EndpointDiscovered.class));
    }

    /**
     * Test case for {@link IntentNotificationSupplierImpl#addEventListener(IEventListener)}
     */
    @Test
    public void addEventListenerTest() {
        IEventListener<?> mockListener = mock(IEventListener.class);
        mockSupplier.addEventListener(mockListener);
        verify(mockRegistryService).registerEventListener(mockSupplier, mockListener);
    }

    /**
     * Test case for {@link IntentNotificationSupplierImpl#removeEventListener(IEventListener)}
     */
    @Test
    public void removeEventListenerTest() {
        IEventListener<?> mockListener = mock(IEventListener.class);
        mockSupplier.removeEventListener(mockListener);
        verify(mockRegistryService).unregisterEventListener(mockSupplier, mockListener);
    }

    /**
     * Test case for {@link IntentNotificationSupplierImpl#close()}
     */
    @Test
    public void closeTest() {
        mockSupplier.close();
    }
}
