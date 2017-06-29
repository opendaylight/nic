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
import org.opendaylight.nic.engine.service.api.EngineService;
import org.opendaylight.nic.engine.api.TransactionHandlerService;
import org.opendaylight.nic.engine.api.StateMachine;
import org.opendaylight.nic.engine.service.impl.*;
import org.opendaylight.nic.utils.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class StateMachineEngineImpl implements StateMachine {

    private static final Logger LOG = LoggerFactory.getLogger(StateMachineEngineImpl.class);
    private static Map<Intent.State, EngineService> engineServiceMap;

    private static StateMachine engineService;

    private static TransactionHandlerService transactionHandlerService;

    public StateMachineEngineImpl(final TransactionHandlerService transactionHandlerService) {
        this.transactionHandlerService = transactionHandlerService;
        engineService = this;
        engineServiceMap = new HashMap<>();
    }

    @Override
    public void start() {
        LOG.info("\nState Machine service Initiated");
//        populate();
    }

    private void populate() {
        engineServiceMap.put(Intent.State.UNDEPLOYED, UndeployedServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.DEPLOYING, DeployingServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.DEPLOYED, DeployedServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.DEPLOYFAILED, DeployingFailedServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.UNDEPLOYING, UndeployingServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.UNDEPLOYFAILED, UndeployFailedServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.DISABLING, DisablingServiceImpl.getInstance(engineService));
    }

    public CheckedFuture<Void, TransactionCommitFailedException> changeState(final IntentStateTransaction transaction) {
        final Intent.State currentState = Intent.State.valueOf(transaction.getCurrentState());
        final EngineService currentService = engineServiceMap.get(currentState);
        CheckedFuture<Void, TransactionCommitFailedException> result = null;
        if(currentService != null) {
            final EventType eventType = EventType.valueOf(transaction.getReceivedEvent());
            result = currentService.execute(eventType, transaction);
        }
        return result;
    }

    @Override
    public CheckedFuture<Void, TransactionCommitFailedException> execute(IntentStateTransaction transaction) {
        return changeState(transaction);
    }

    @Override
    public CheckedFuture<Void, TransactionCommitFailedException> changeTransactionState(final IntentStateTransaction transaction) {
        return transactionHandlerService.storeStateChange(transaction);
    }

    @Override
    public CheckedFuture<Void, TransactionCommitFailedException> pushTransaction(final IntentStateTransaction transaction) {
        return transactionHandlerService.sendTransaction(transaction);
    }

    @Override
    public boolean canExecute(String id, EventType eventType) {
        final IntentStateTransaction transaction = transactionHandlerService.retrieveTransaction(id);
        boolean result = true;
        int deployAttempts = transaction.getDeployAttempts();
        int undeployAttempts = transaction.getUndeployAttempts();
        switch (eventType) {
            case INTENT_ADD_ATTEMPT:
                if (deployAttempts == 0) {
                    result = false;
                }
                break;
            case INTENT_REMOVE_ATTEMPT:
                if (undeployAttempts == 0) {
                    result = false;
                }
                break;
        }
        return result;
    }

    @Override
    public IntentStateTransaction retrieveTransaction(String id) {
        return transactionHandlerService.retrieveTransaction(id);
    }

    @Override
    public void stop() {
        //TODO: Provide cleanup implementation
    }
}
