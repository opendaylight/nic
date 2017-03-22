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
import org.opendaylight.nic.common.transaction.api.IntentCommonService;
import org.opendaylight.nic.engine.IntentStateMachineExecutorService;
import org.opendaylight.nic.listeners.api.*;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;

import org.opendaylight.yang.gen.v1.urn.onf.intent.nbi.rev160920.intent.definitions.IntentDefinition;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.groups.attributes.security.groups.SecurityGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.rules.attributes.security.rules.SecurityRule;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.IntentStateTransactions;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import java.util.ArrayList;
import java.util.List;

/**
 * Provider Implementation for NIC events
 */
public class ListenerProviderImpl implements AutoCloseable {

    private final DataBroker db;

    /* Supplier List property help for easy close method implementation and testing */
    private List<NotificationSupplierDefinition<?>> supplierList;
    private EventRegistryServiceImpl serviceRegistry = null;
    private NotificationService notificationService;
    private OFRendererFlowService flowService;
    private IntentCommonService intentCommonService;
    private IntentStateMachineExecutorService stateMachineExecutorService;
    private OFRendererGraphService graphService;
    private EndpointDiscoveredNotificationSupplierImpl endpointResolver;
    private MdsalUtils mdsalUtils;

    /**
     * Provider constructor set all needed final parameters
     * @param db The {@link DataBroker}
     * @param notificationService The {@link NotificationService} used with pub-sub pattern
     * @param flowService The {@link OFRendererFlowService} used to render and push OF Rules
     * @param graphService The {@link OFRendererGraphService} used to represent and solve a
     * @param intentCommonService The {@link IntentCommonService} used to translate Intents into FlowData for renders
     *                     Network-Topology.
     */
    public ListenerProviderImpl(final DataBroker db,
                                NotificationService notificationService,
                                OFRendererFlowService flowService,
                                OFRendererGraphService graphService,
                                IntentCommonService intentCommonService,
                                IntentStateMachineExecutorService stateMachineExecutorService) {
        Preconditions.checkNotNull(db);
        Preconditions.checkNotNull(notificationService);
        Preconditions.checkNotNull(flowService);
        Preconditions.checkNotNull(intentCommonService);
        this.db = db;
        this.notificationService = notificationService;
        this.flowService = flowService;
        this.graphService = graphService;
        this.mdsalUtils = new MdsalUtils(db);
        this.intentCommonService = intentCommonService;
        this.stateMachineExecutorService = stateMachineExecutorService;
    }

    public void start() {
        serviceRegistry = new EventRegistryServiceImpl();

        // Event providers
        NotificationSupplierForItemRoot<IntentLimiter, IntentLimiterAdded, IntentLimiterRemoved, IntentLimiterUpdated> intentLimiterSupp =
                new IntentLimiterNotificationSupplierImpl(db);
        NotificationSupplierForItemRoot<FlowCapableNode, NodeUp, NodeDeleted, NodeUpdated> nodeSupp =
                new NodeNotificationSupplierImpl(db);
        NotificationSupplierForItemRoot<FlowCapableNodeConnector, LinkUp, LinkDeleted, NicNotification> connectorSupp =
                new NodeConnectorNotificationSupplierImpl(db);
        NotificationSupplierForItemRoot<Intent, IntentAdded, IntentRemoved, IntentUpdated> intentSupp =
                new IntentNotificationSupplierImpl(db);
        NotificationSupplierForItemRoot<IntentDefinition, IntentNBIAdded, IntentNBIRemoved, IntentNBIUpdated> intentNBISupp =
                new IntentNBINotificationSupplierImpl(db);
        NotificationSupplierForItemRoot<SecurityGroup, SecurityGroupAdded, SecurityGroupDeleted, SecurityGroupUpdated> secGroupSupp =
                new NeutronSecGroupNotificationSupplierImpl(db);
        NotificationSupplierForItemRoot<SecurityRule, SecurityRuleAdded, SecurityRuleDeleted, SecurityRuleUpdated> secRulesSupp =
                new NeutronSecRuleNotificationSupplierImpl(db);
        NotificationSupplierForItemRoot<Link, TopologyLinkUp, TopologyLinkDeleted, NicNotification> linkSupp =
                new TopologyLinkNotificationSupplierImpl(db);
        NotificationSupplierForSingleItem<IntentStateTransactions, IntentStateChanged> intentStateTransactionSupp =
                new TransactionStateNotificationSuplierImpl(db);
        endpointResolver = new EndpointDiscoveredNotificationSupplierImpl(notificationService);

        // Event listeners
        IntentLimiterNotificationSubscriberImpl intentLimiterListener = new IntentLimiterNotificationSubscriberImpl(intentCommonService, stateMachineExecutorService);
        IntentNotificationSubscriberImpl intentListener = new IntentNotificationSubscriberImpl(intentCommonService,
                stateMachineExecutorService);
        IntentNBINotificationSubscriberImpl intentNBIListener = new IntentNBINotificationSubscriberImpl(flowService);
        EndpointDiscoveryNotificationSubscriberImpl endpointDiscoverySubscriber =
                new EndpointDiscoveryNotificationSubscriberImpl();
        NodeNotificationSubscriberImpl nodeNotifSubscriber = new NodeNotificationSubscriberImpl(intentCommonService);
        TopologyLinkNotificationSubscriberImpl topologyLinkNotifSubscriber =
                new TopologyLinkNotificationSubscriberImpl(graphService, mdsalUtils);
        TransactionStateNotificationSubscriberImpl stateNotificationSubscriber =
                new TransactionStateNotificationSubscriberImpl(intentCommonService);
        serviceRegistry.registerEventListener((IEventService) intentLimiterSupp, intentLimiterListener);
        serviceRegistry.registerEventListener((IEventService) intentSupp, intentListener);
        serviceRegistry.registerEventListener((IEventService) intentNBISupp, intentNBIListener);
        serviceRegistry.registerEventListener((IEventService) nodeSupp, nodeNotifSubscriber);
        serviceRegistry.registerEventListener(endpointResolver, endpointDiscoverySubscriber);
        serviceRegistry.registerEventListener((IEventService) linkSupp, topologyLinkNotifSubscriber);
        serviceRegistry.registerEventListener((IEventService) intentStateTransactionSupp, stateNotificationSubscriber);

        supplierList = new ArrayList<>();
        supplierList.add(intentLimiterSupp);
        supplierList.add(nodeSupp);
        supplierList.add(connectorSupp);
        supplierList.add(intentSupp);
        supplierList.add(intentNBISupp);
        supplierList.add(secGroupSupp);
        supplierList.add(secRulesSupp);
        supplierList.add(linkSupp);
        supplierList.add(intentStateTransactionSupp);
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