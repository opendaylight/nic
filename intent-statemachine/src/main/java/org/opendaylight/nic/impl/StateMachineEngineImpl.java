/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP, Serro LCC and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.impl;

import org.opendaylight.nic.engine.StateMachineEngineService;
import org.opendaylight.nic.engine.impl.*;
import org.opendaylight.nic.engine.service.EngineService;
import org.opendaylight.nic.engine.service.TransactionHandlerService;
import org.opendaylight.nic.utils.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class StateMachineEngineImpl implements StateMachineEngineService {

    private static final Logger LOG = LoggerFactory.getLogger(StateMachineEngineImpl.class);
    private static Map<Intent.State, EngineService> engineServiceMap;

    private static EventType eventType;

    private static String intentId;

    private static StateMachineEngineService engineService;

    private static TransactionHandlerService transactionHandlerService;

    public StateMachineEngineImpl(final TransactionHandlerService transactionHandlerService) {
        this.transactionHandlerService = transactionHandlerService;
        engineService = this;
        engineServiceMap = new HashMap<>();
        populate();
    }

    private void populate() {
        engineServiceMap.put(Intent.State.UNDEPLOYED, DeployServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.DEPLOYING, DeployServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.DEPLOYFAILED, DeployFailedServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.UNDEPLOYING, UndeployServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.UNDEPLOYFAILED, UndeployFailedServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.DISABLING, DisableServiceImpl.getInstance(engineService));
    }

    public void changeState(final Intent.State currentState) {
        final EngineService currentService = engineServiceMap.get(currentState);
        if(currentService != null) {
            currentService.execute(eventType);
        }
    }

    @Override
    public void execute(IntentStateTransaction transaction) {
        final String event = transaction.getReceivedEvent();
        final String currentState = transaction.getCurrentState();
        this.eventType = EventType.valueOf(event);
        this.intentId = transaction.getIntentId().toString();
        changeState(Intent.State.valueOf(currentState));
    }

    @Override
    public void changeTransactionState(final String newState) {
        transactionHandlerService.storeStateChange(intentId, newState, eventType.toString());
    }

    @Override
    public void pushTransaction(final IntentStateTransaction transaction) {
        transactionHandlerService.sendTransaction(transaction);
    }
}
