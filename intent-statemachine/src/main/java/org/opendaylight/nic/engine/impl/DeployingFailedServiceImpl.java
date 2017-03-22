/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise; Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.impl;

import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.nic.engine.StateMachine;
import org.opendaylight.nic.engine.service.DeployFailedService;
import org.opendaylight.nic.engine.utils.StateMachineUtils;
import org.opendaylight.nic.utils.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;

public class DeployingFailedServiceImpl implements DeployFailedService {

    private static DeployFailedService deployFailedService;
    private static StateMachine engineService;

    private DeployingFailedServiceImpl(StateMachine engineService) {
        this.engineService = engineService;

    }

    public static DeployFailedService getInstance(StateMachine engineService) {
        if (deployFailedService == null) {
            deployFailedService = new DeployingFailedServiceImpl(engineService);
        }
        return deployFailedService;
    }

    @Override
    public CheckedFuture<Void, TransactionCommitFailedException> execute(final EventType eventType,
                                                                         final IntentStateTransaction transaction) {
        StateMachineUtils.waitTransactionFinish();
        final IntentStateTransaction newTransaction;
        final Intent.State newState = getStateByEvent(eventType);
        newTransaction = StateMachineUtils.buildNewTransactionBy(transaction,newState, eventType);
        return engineService.changeTransactionState(newTransaction);
    }

    private Intent.State getStateByEvent(final EventType eventType) {
        Intent.State result;

        switch (eventType) {
            case INTENT_BEING_ADDED:
            case INTENT_ADD_ATTEMPT:
                result = Intent.State.DEPLOYING;
                break;
            case INTENT_ADDED_ERROR:
                result = Intent.State.DISABLING;
                break;
            case INTENT_ADD_RETRY_WITH_MAX_ATTEMPTS:
                result = Intent.State.DEPLOYFAILED;
                break;
            default:
                result = Intent.State.DISABLING;
                break;
        }
        return result;
    }
}
