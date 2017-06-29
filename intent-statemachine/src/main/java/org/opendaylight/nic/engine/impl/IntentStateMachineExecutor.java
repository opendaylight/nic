/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP, Serro LLC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.impl;

import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.nic.engine.api.IntentStateMachineExecutorService;
import org.opendaylight.nic.engine.api.StateMachine;
import org.opendaylight.nic.engine.utils.StateMachineUtils;
import org.opendaylight.nic.utils.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransactionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntentStateMachineExecutor implements IntentStateMachineExecutorService {
    private static final Logger LOG = LoggerFactory.getLogger(IntentStateMachineExecutor.class);

    private StateMachine stateMachine;

    public IntentStateMachineExecutor(final StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @Override
    public void start() {
        LOG.info("Intent State Machine Session Initiated.");
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
        final CheckedFuture<Void, TransactionCommitFailedException> checkedFuture = stateMachine.pushTransaction(transaction);
        return checkedFuture;
    }

    @Override
    public synchronized CheckedFuture<Void, TransactionCommitFailedException> goToNextTransaction(String intentId, EventType eventType){
        final IntentStateTransaction transaction = stateMachine.retrieveTransaction(intentId);
        final IntentStateTransaction newTransaction =
                StateMachineUtils.buildNewTransactionBy(transaction, Intent.State.valueOf(transaction.getCurrentState()), eventType);
        return stateMachine.execute(newTransaction);
    }

    @Override
    public synchronized void removeTransactions(String intentId, EventType receivedEvent) {
        final IntentStateTransaction transaction = stateMachine.retrieveTransaction(intentId);
        stateMachine.execute(new IntentStateTransactionBuilder(transaction).setReceivedEvent(receivedEvent.name()).build());
    }

    @Override
    public synchronized boolean canEvaluateAttempt(String id, EventType eventType) {
        return stateMachine.canExecute(id, eventType);
    }

    @Override
    public void stop() {
        LOG.info("\nStopping Intent State Machine session");
    }
}
