/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.engine.IntentStateMachineExecutorService;
import org.opendaylight.nic.engine.StateMachineEngineService;
import org.opendaylight.nic.utils.MdsalUtils;
import transaction.api.EventType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.transaction.rev151203.IsmTransactions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.transaction.rev151203.ism.transactions.IntentTransaction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.transaction.rev151203.ism.transactions.IntentTransactionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.transaction.rev151203.ism.transactions.IntentTransactionKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import transaction.api.IntentTransactionNotifier;
import transaction.api.IntentTransactionRegistryService;
import transaction.impl.IntentTransactionResultListener;

import java.util.List;

//TODO: In the future, this class must use the MD-SAL to execute transactions
public class IntentStateMachineExecutor implements IntentStateMachineExecutorService, IntentTransactionResultListener {
    private static final Logger LOG = LoggerFactory.getLogger(IntentStateMachineExecutor.class);

    private ServiceRegistration<IntentStateMachineExecutorService> nicStateMachineServiceRegistration;

    private IntentTransactionRegistryService transactionRegistryService;
    private IntentTransactionNotifier transactionNotifier;

    private MdsalUtils mdsalUtils;
    private DataBroker dataBroker;
    private static final String CREATE_TRANSACTION_EXCEPTION_MSG = "Error when try to create a new transaction for Intent with ID :";

    public IntentStateMachineExecutor(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public void init() {
        LOG.info("Intent State Machine Session Initiated.");
        this.mdsalUtils = new MdsalUtils(dataBroker);
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<?> serviceReference = context
                .getServiceReference(IntentTransactionRegistryService.class);
        nicStateMachineServiceRegistration = context.registerService(IntentStateMachineExecutorService.class, this, null);
        transactionRegistryService = (IntentTransactionRegistryService) context.getService(serviceReference);

        ServiceReference<?> serviceReferenceNotifier = context
                .getServiceReference(IntentTransactionNotifier.class);
        transactionRegistryService.registerForResults(this);
        transactionNotifier = (IntentTransactionNotifier) context.getService(serviceReferenceNotifier);
    }

    @Override
    public void createTransaction(Uuid intentId, EventType receivedEvent) {
        final IntentTransactionBuilder builder = retrieveTransactionBuilder(intentId, receivedEvent);
        InstanceIdentifier<IntentTransaction> identifier;
        identifier = createIdentifier(intentId);
        final StateMachineEngineService engineService =
                new StateMachineEngineImpl(builder, mdsalUtils, identifier);
        engineService.execute();
    }

    @Override
    public void removeTransactions(Uuid intentId, EventType receivedEvent) {

        //TODO: Use the queue on MD-SAL to remove ready transactions
        //tagged with this intentId
    }

    @Override
    public List<Intent> getUndeployedIntents(IpAddress ipAddress) {
        //InstanceIdentifier<IntentTransaction> identifier = createIdentifier();
        //IntentTransaction transaction = mdsalUtils.read(LogicalDatastoreType.OPERATIONAL, identifier);
        //TODO: Return all Undeployed Intents from the queue that does match with this IPAddress
        return null;
    }

    @Override
    public void close() throws Exception {
        nicStateMachineServiceRegistration.unregister();
    }

    private InstanceIdentifier<IntentTransaction> createIdentifier(Uuid intentId) {
        //TODO: Fix it!
        InstanceIdentifier<IntentTransaction> identifier;
        identifier = InstanceIdentifier
                .builder(IsmTransactions.class)
                .child(IntentTransaction.class, new IntentTransactionKey(intentId)).build();
        return identifier;
    }

    @Override
    public void deployFailure(Uuid intentId) {
        createTransaction(intentId, EventType.INTENT_DEPLOY_FAILURE);
    }

    @Override
    public void deploySuccess(Uuid intentId) {
        createTransaction(intentId, EventType.INTENT_DEPLOY_SUCCESS);
    }

    private IntentTransactionBuilder retrieveTransactionBuilder(final Uuid intentId, EventType eventType) {
        IntentTransactionBuilder builder;
        final InstanceIdentifier<IntentTransaction> identifier = createIdentifier(intentId);
        final IntentTransaction transaction = mdsalUtils.read(LogicalDatastoreType.OPERATIONAL, identifier);

        if(transaction != null) {
            builder = new IntentTransactionBuilder(transaction);
        } else {
            builder = new IntentTransactionBuilder();
            builder.setCurrentState(Intent.State.UNDEPLOYED.getName());
        }
        builder.setNetworkEvent(eventType.toString());
        return builder;
    }
}
