/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;

import org.opendaylight.nic.listeners.api.IntentAdded;
import org.opendaylight.nic.listeners.api.LinkUp;
import org.opendaylight.nic.listeners.api.NodeDeleted;
import org.opendaylight.nic.listeners.api.NodeUp;
import org.opendaylight.nic.listeners.api.NotificationSupplierForItemRoot;
import org.opendaylight.nic.listeners.api.LinkDeleted;
import org.opendaylight.nic.listeners.api.IntentRemoved;
import org.opendaylight.nic.listeners.api.IEventService;
import org.opendaylight.nic.listeners.api.NotificationSupplierDefinition;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provider Implementation for NIC events
 */
public class ListenerProviderImpl implements AutoCloseable {

    private final DataBroker db;

    /* Supplier List property help for easy close method implementation and testing */
    private List<NotificationSupplierDefinition<?>> supplierList;
    private NotificationSupplierForItemRoot<FlowCapableNode, NodeUp, NodeDeleted> nodeSupp;
    private NotificationSupplierForItemRoot<FlowCapableNodeConnector,
            LinkUp, LinkDeleted> connectorSupp;
    private NotificationSupplierForItemRoot<Intent, IntentAdded, IntentRemoved> intentSupp;
    private static EventServiceRegistry serviceRegistry = EventServiceRegistry.getInstance();
    private OFRendererFlowService flowService;
    private NotificationService notificationService;
    private ListenerRegistration<EndpointResolver> notificationListenerRegistration = null;

    /**
     * Provider constructor set all needed final parameters
     *
     * @param db - dataBroker
     */
    public ListenerProviderImpl(final DataBroker db, NotificationService notificationService) {
        Preconditions.checkNotNull(db);
        Preconditions.checkNotNull(notificationService);
        this.db = db;
        this.notificationService = notificationService;
    }

    public void start() {
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<?> serviceReference = context.
                getServiceReference(OFRendererFlowService.class);
        flowService = (OFRendererFlowService) context.
                getService(serviceReference);

        // Event providers
        nodeSupp = new NodeNotificationSupplierImpl(db);
        connectorSupp = new NodeConnectorNotificationSupplierImpl(db);
        intentSupp = new IntentNotificationSupplierImpl(db);

        // Event listeners
        IntentNotificationSubscriberImpl intentListener = new IntentNotificationSubscriberImpl(flowService);
        serviceRegistry.registerEventListener((IEventService) intentSupp, intentListener);
        NodeNotificationSubscriberImpl nodeNotifSubscriber = new NodeNotificationSubscriberImpl(flowService);
        serviceRegistry.registerEventListener((IEventService) nodeSupp, nodeNotifSubscriber);

        EndpointResolver endpointResolver = new EndpointResolver();
        notificationListenerRegistration = notificationService.registerNotificationListener(endpointResolver);
        supplierList = new ArrayList<NotificationSupplierDefinition<?>>(Arrays.asList(nodeSupp));
        supplierList.add(nodeSupp);
        supplierList.add(connectorSupp);
        supplierList.add(intentSupp);
    }

    @Override
    public void close() throws Exception {
        if (notificationListenerRegistration != null) {
            notificationListenerRegistration.close();
        }
        for (NotificationSupplierDefinition<?> supplier : supplierList) {
            if (supplier != null) {
                supplier.close();
            }
        }
    }

    public List<NotificationSupplierDefinition<?>> getSupplierList() {
        return supplierList;
    }
}

