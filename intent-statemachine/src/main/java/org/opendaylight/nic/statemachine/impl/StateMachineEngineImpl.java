/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.statemachine.impl;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.statemachine.engine.StateMachineEngineService;
import org.opendaylight.nic.statemachine.engine.impl.DeployFailedServiceImpl;
import org.opendaylight.nic.statemachine.engine.impl.DeployServiceImpl;
import org.opendaylight.nic.statemachine.engine.impl.DeploySuccessServiceImpl;
import org.opendaylight.nic.statemachine.engine.impl.DisableServiceImpl;
import org.opendaylight.nic.statemachine.engine.impl.UndeployFailedServiceImpl;
import org.opendaylight.nic.statemachine.engine.impl.UndeployServiceImpl;
import org.opendaylight.nic.statemachine.engine.impl.UndeployedServiceImpl;
import org.opendaylight.nic.statemachine.engine.service.EngineService;
import org.opendaylight.nic.transaction.api.EventType;
import org.opendaylight.nic.utils.MdsalUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.transaction.rev151203.ism.transactions.IntentTransaction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.transaction.rev151203.ism.transactions.IntentTransactionBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.HashMap;
import java.util.Map;

public class StateMachineEngineImpl implements StateMachineEngineService {

    private static Map<Intent.State, EngineService> engineServiceMap;

    private static EventType eventType;

    private static StateMachineEngineService engineService;

    private static IntentTransaction intentTransaction;

    private static IntentTransactionBuilder transactionBuilder;

    private static MdsalUtils mdsalUtils;

    private static InstanceIdentifier<IntentTransaction> identifier;

    public StateMachineEngineImpl(IntentTransactionBuilder transactionBuilder,
                                  MdsalUtils mdsalUtils,
                                  InstanceIdentifier<IntentTransaction> identifier) {
        engineService = this;
        engineServiceMap = new HashMap<>();
        this.transactionBuilder = transactionBuilder;
        this.mdsalUtils = mdsalUtils;
        this.identifier = identifier;
        populate();
    }

    private void populate() {
        //TODO: Create an IMPL for DEPLOYSUCCESS, UNDEPLOY and DISABLE
        engineServiceMap.put(Intent.State.DEPLOYING, DeployServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.DEPLOYED, DeploySuccessServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.DEPLOYFAILED, DeployFailedServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.UNDEPLOYED, UndeployedServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.UNDEPLOYING, UndeployServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.UNDEPLOYFAILED, UndeployFailedServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.DISABLING, DisableServiceImpl.getInstance(engineService));
    }

    public void changeState(final Intent.State currentState) {
        updateTransaction(currentState);
        final EngineService currentService = engineServiceMap.get(currentState);
        if (currentService != null) {
            currentService.execute(eventType);
        }
    }

    @Override
    public void updateTransaction(Intent.State currentState) {
        transactionBuilder.setCurrentState(currentState.toString());
        mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, identifier, transactionBuilder.build());
    }

    @Override
    public Uuid getIntentId() {
        return intentTransaction.getIntentId();
    }

    @Override
    public void execute() {
        this.intentTransaction = transactionBuilder.build();
        this.eventType = EventType.valueOf(intentTransaction.getNetworkEvent());
        final Intent.State currentState = Intent.State.valueOf(intentTransaction.getCurrentState());
        changeState(currentState);
    }

    @Override
    public void execute(IntentTransaction intentTransaction) {
        this.transactionBuilder = new IntentTransactionBuilder(intentTransaction);
        final Intent.State currentState = Intent.State.valueOf(intentTransaction.getCurrentState());
        this.eventType = EventType.valueOf(intentTransaction.getNetworkEvent());
        changeState(currentState);
    }
}
