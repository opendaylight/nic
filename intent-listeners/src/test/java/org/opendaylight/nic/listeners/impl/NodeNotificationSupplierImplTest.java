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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
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
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({ Bundle.class, DataBroker.class, NodeNotificationSupplierImpl.class,
        FrameworkUtil.class, BundleContext.class})
@RunWith(PowerMockRunner.class)
/**
 * JUnit test for {@link NodeNotificationSupplierImpl}.
 */
public class NodeNotificationSupplierImplTest {
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
        private NodeNotificationSupplierImpl mockNodeSupplier;
        /**
         * Mock instance of FlowCapableNode to perform unit testing.
         */
        private FlowCapableNode mockNode;
        /**
         * Mock instance of InstanceIdentifier<FlowCapableNode> to perform unit testing.
         */
        private InstanceIdentifier<FlowCapableNode> mockInstanceIdentifier;

        @Before
        public void setup() {
                /**
                 * Create required mock objects and define mocking functionality
                 * for mock objects.
                 */
                mockDataBroker = mock(DataBroker.class);
                mockRegistryService = mock(EventRegistryService.class);
                mockNode = mock(FlowCapableNode.class);
                mockInstanceIdentifier= mock(InstanceIdentifier.class);
                Bundle mockBundle = mock(Bundle.class);
                BundleContext mockBundleContext = mock(BundleContext.class);
                ServiceReference<EventRegistryService> mockServiceReference = mock(ServiceReference.class);
                PowerMockito.mockStatic(FrameworkUtil.class);
                when(FrameworkUtil.getBundle(NodeNotificationSupplierImpl.class)).thenReturn(mockBundle);
                when(mockBundle.getBundleContext()).thenReturn(mockBundleContext);
                when(mockBundleContext.getServiceReference(EventRegistryService.class)).thenReturn(mockServiceReference);
                when(mockBundleContext.getService(mockServiceReference)).thenReturn(mockRegistryService);
                mockNodeSupplier = PowerMockito.spy(new NodeNotificationSupplierImpl(mockDataBroker));
                verify(mockRegistryService).setEventTypeService(any(NodeNotificationSupplierImpl.class),
                        eq(EventType.NODE_UPDATED), eq(EventType.NODE_REMOVED), eq(EventType.NODE_UPDATED));
        }

        /**
         * Test case for {@link NodeNotificationSupplierImpl#createNotification(FlowCapableNode, InstanceIdentifier)}
         */
        @Test
        public void createNotificationTest() throws Exception {
                NodeId mockNodeId = mock(NodeId.class);
                NodeKey mockNodeKey = new NodeKey(mockNodeId);
                FlowCapableNode mockFcn = mock(FlowCapableNode.class);
                InstanceIdentifier<FlowCapableNode> fcnIid = InstanceIdentifier
                                                                 .builder(Nodes.class)
                                                                 .child(Node.class, mockNodeKey)
                                                                 .augmentation(FlowCapableNode.class)
                                                                 .build();
                NodeUp mockNodeUp = mockNodeSupplier.createNotification(mockFcn, fcnIid);
                assertNotNull(mockNodeUp);
                assertEquals(mockNodeUp.getIp(),mockNode.getIpAddress());
                assertEquals(mockNodeUp.getNodeId(), mockNodeId);
        }

        /**
         * Test case for {@link NodeNotificationSupplierImpl#deleteNotification(FlowCapableNode, InstanceIdentifier)}
         */
        @Test
        public void deleteNotificationTest() {
                NodeDeleted mockNodeDeleted = mockNodeSupplier.deleteNotification(mockNode, mockInstanceIdentifier);
                assertNotNull(mockNodeDeleted);
                assertEquals(mockNodeDeleted.getNodeRef().getValue(), mockInstanceIdentifier);
        }

        /**
         * Test case for {@link NodeNotificationSupplierImpl#updateNotification(FlowCapableNode, InstanceIdentifier)}
         */
        @Test
        public void updateNotificationTest() {
                NodeUpdated mockNodeUpdated = mockNodeSupplier.updateNotification(mockNode, mockInstanceIdentifier);
                assertNotNull(mockNodeUpdated);
                assertEquals(mockNodeUpdated.getNodeRef().getValue(), mockInstanceIdentifier);
        }

        /**
         * Test case for {@link NodeNotificationSupplierImpl#addEventListener(IEventListener)}
         */
        @Test
        public void addEventListenerTest() {
                IEventListener<?> mockListener = mock(IEventListener.class);
                mockNodeSupplier.addEventListener(mockListener);
                verify(mockRegistryService).registerEventListener(mockNodeSupplier, mockListener);
        }

        /**
         * Test case for {@link NodeNotificationSupplierImpl#removeEventListener(IEventListener)}
         */
        @Test
        public void removeEventListenerTest() {
                IEventListener<?> mockListener = mock(IEventListener.class);
                mockNodeSupplier.removeEventListener(mockListener);
                verify(mockRegistryService).unregisterEventListener(mockNodeSupplier, mockListener);
        }
}
