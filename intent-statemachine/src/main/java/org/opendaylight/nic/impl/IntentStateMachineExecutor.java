/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP, Serro LLC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.impl;

import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.nic.engine.IntentStateMachineExecutorService;
import org.opendaylight.nic.engine.StateMachine;
import org.opendaylight.nic.engine.utils.StateMachineUtils;
import org.opendaylight.nic.utils.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransactionBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntentStateMachineExecutor implements IntentStateMachineExecutorService {
    private static final Logger LOG = LoggerFactory.getLogger(IntentStateMachineExecutor.class);

    private ServiceRegistration<IntentStateMachineExecutorService> nicStateMachineServiceRegistration;
    private DataBroker dataBroker;

    public IntentStateMachineExecutor(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public void init() {
        LOG.info("Intent State Machine Session Initiated.");
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        nicStateMachineServiceRegistration = context.registerService(IntentStateMachineExecutorService.class, this, null);

    }

    @Override
    public synchronized CheckedFuture<Void, TransactionCommitFailedException> createTransaction(
            final String intentId,
            final EventType receivedEvent) {
        final IntentStateTransaction transaction = new IntentStateTransactionBuilder()
                .setIntentId(intentId)
                .setDeployAttempts(StateMachineUtils.MAX_ATTEMPTS)
                .setUndeployAttempts(StateMachineUtils.MAX_ATTEMPTS)
                .setCurrentState(StateMachineUtils.INITIAL_STATE)
                .setReceivedEvent(receivedEvent.toString()).build();
        final StateMachine engineService =
                new StateMachineEngineImpl(new TransactionHandlerServiceImpl(dataBroker));
        final CheckedFuture<Void, TransactionCommitFailedException> checkedFuture = engineService.pushTransaction(transaction);
        return checkedFuture;
    }

    @Override
    public synchronized CheckedFuture<Void, TransactionCommitFailedException> goToNextTransaction(String intentId, EventType eventType){
        final StateMachine engineService = getEngineService();
        final IntentStateTransaction transaction = engineService.retrieveTransaction(intentId);
        final IntentStateTransaction newTransaction =
                StateMachineUtils.buildNewTransactionBy(transaction, Intent.State.valueOf(transaction.getCurrentState()), eventType);
        return engineService.execute(newTransaction);
    }

    @Override
    public synchronized void removeTransactions(String intentId, EventType receivedEvent) {
        final StateMachine engineService = getEngineService();
        final IntentStateTransaction transaction = engineService.retrieveTransaction(intentId);
        engineService.execute(new IntentStateTransactionBuilder(transaction).setReceivedEvent(receivedEvent.name()).build());
    }

    @Override
    public synchronized boolean canEvaluateAttempt(String id, EventType eventType) {
        return getEngineService().canExecute(id, eventType);
    }

    private StateMachine getEngineService() {
        return new StateMachineEngineImpl(new TransactionHandlerServiceImpl(dataBroker));
    }

    @Override
    public void close() throws Exception {
        nicStateMachineServiceRegistration.unregister();
    }
}
