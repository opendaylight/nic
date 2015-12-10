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
import org.opendaylight.controller.md.sal.binding.api.NotificationService;
import org.opendaylight.nic.listeners.api.*;
import org.opendaylight.nic.of.renderer.api.OFRenderedGraphService;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.groups.attributes.security.groups.SecurityGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.rules.attributes.security.rules.SecurityRule;
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
    private  EventRegistryServiceImpl serviceRegistry = null;
    private NotificationService notificationService;

    private EndpointDiscoveredNotificationSupplierImpl endpointResolver;

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
        // Retrieve reference for OFRenderer service
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<?> serviceReference = context.
                getServiceReference(OFRendererFlowService.class);
        ServiceReference<?> graphServiceReference = context.
                getServiceReference(OFRenderedGraphService.class);
        OFRendererFlowService flowService = (OFRendererFlowService) context.
                getService(serviceReference);
        OFRenderedGraphService graphService = (OFRenderedGraphService) context
                .getService(graphServiceReference);
        serviceRegistry = new EventRegistryServiceImpl();

        // Event providers
        NotificationSupplierForItemRoot<FlowCapableNode, NodeUp, NodeDeleted, NodeUpdated> nodeSupp = new NodeNotificationSupplierImpl(db);
        NotificationSupplierForItemRoot<FlowCapableNodeConnector, LinkUp, LinkDeleted, NicNotification> connectorSupp = new NodeConnectorNotificationSupplierImpl(db);
        NotificationSupplierForItemRoot<Intent, IntentAdded, IntentRemoved, IntentUpdated> intentSupp = new IntentNotificationSupplierImpl(db);
        NotificationSupplierForItemRoot<SecurityGroup, SecurityGroupAdded, SecurityGroupDeleted, SecurityGroupUpdated> secGroupSupp =
                new NeutronSecGroupNotificationSupplierImpl(db);
        NotificationSupplierForItemRoot<SecurityRule, SecurityRuleAdded, SecurityRuleDeleted, SecurityRuleUpdated> secRulesSupp =
                new NeutronSecRuleNotificationSupplierImpl(db);
        endpointResolver = new EndpointDiscoveredNotificationSupplierImpl(notificationService);

        // Event listeners
        IntentNotificationSubscriberImpl intentListener = new IntentNotificationSubscriberImpl(flowService);
        serviceRegistry.registerEventListener((IEventService) intentSupp, intentListener);
        NodeNotificationSubscriberImpl nodeNotifSubscriber = new NodeNotificationSubscriberImpl(flowService,
                                                                                                graphService);
        serviceRegistry.registerEventListener((IEventService) nodeSupp, nodeNotifSubscriber);
        EndpointDiscoveryNotificationSubscriberImpl endpointDiscoverySubscriber =
                new EndpointDiscoveryNotificationSubscriberImpl();
        serviceRegistry.registerEventListener(endpointResolver, endpointDiscoverySubscriber);

        supplierList = new ArrayList<NotificationSupplierDefinition<?>>(Arrays.asList(nodeSupp));
        supplierList.add(nodeSupp);
        supplierList.add(connectorSupp);
        supplierList.add(intentSupp);
        supplierList.add(secGroupSupp);
        supplierList.add(secRulesSupp);
    }

    @Override
    public void close() throws Exception {
        endpointResolver.close();
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