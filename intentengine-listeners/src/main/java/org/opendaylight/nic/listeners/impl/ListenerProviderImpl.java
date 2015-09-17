/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;

import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.nic.listeners.api.IntentRemoved;
import org.opendaylight.nic.listeners.api.LinkUp;
import org.opendaylight.nic.listeners.api.NodeUp;
import org.opendaylight.nic.listeners.api.NodeDeleted;
import org.opendaylight.nic.listeners.api.LinkDeleted;
import org.opendaylight.nic.listeners.api.NotificationSupplierForItemRoot;
import org.opendaylight.nic.listeners.api.IntentAdded;
import org.opendaylight.nic.listeners.api.NotificationSupplierDefinition;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provider Implementation for NIC events
 */
public class ListenerProviderImpl {

    private final DataBroker db;

    /* Supplier List property help for easy close method implementation and testing */
    private List<NotificationSupplierDefinition<?>> supplierList;
    private NotificationSupplierForItemRoot<FlowCapableNode, NodeUp, NodeDeleted> nodeSupp;
    private NotificationSupplierForItemRoot<FlowCapableNodeConnector,
            LinkUp, LinkDeleted> connectorSupp;
    private NotificationSupplierForItemRoot<Intent, IntentAdded, IntentRemoved> intentSupp;
    private static EventServiceRegistry serviceRegistry = EventServiceRegistry.getInstance();

    /**
     * Provider constructor set all needed final parameters
     *
     * @param db - dataBroker
     */
    public ListenerProviderImpl(final DataBroker db, RpcProviderRegistry rpcRegistry) {
        Preconditions.checkNotNull(db);
        Preconditions.checkNotNull(rpcRegistry);
        this.db = db;
    }

    public void start() {
        nodeSupp = new NodeNotificationSupplierImpl(db);
        connectorSupp = new NodeConnectorNotificationSupplierImpl(db);
        intentSupp = new IntentNotificationSupplierImpl(db);
        //TODO: Adding listeners for switch events
//        NodeNotificationSubscriberImpl nodeNotifSubscriber = new NodeNotificationSubscriberImpl();
//        serviceRegistry.registerEventListener((IEventService) nodeSupp, nodeNotifSubscriber);
        supplierList = new ArrayList<NotificationSupplierDefinition<?>>(Arrays.asList(nodeSupp));
    }

    public void close() throws Exception {
        for (NotificationSupplierDefinition<?> supplier : supplierList) {
            if (supplier != null) {
                supplier.close();
                supplier = null;
            }
        }
    }

    List<NotificationSupplierDefinition<?>> getSupplierList() {
        return supplierList;
    }
}

