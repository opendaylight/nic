/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.engine.impl;

import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.nic.engine.StateMachine;
import org.opendaylight.nic.engine.service.DeployingService;
import org.opendaylight.nic.engine.utils.StateMachineUtils;
import org.opendaylight.nic.utils.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransactionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yrineu on 10/04/17.
 */
public class DeployingServiceImpl implements DeployingService {
    private static final Logger LOG = LoggerFactory.getLogger(DeployingServiceImpl.class);

    private static StateMachine engineService;
    private static DeployingService deployingService;

    private DeployingServiceImpl(final StateMachine engineService) {
        this.engineService = engineService;
    }

    public static DeployingService getInstance(StateMachine engineService) {
        if (deployingService == null) {
            deployingService = new DeployingServiceImpl(engineService);
        }
        return deployingService;
    }

    @Override
    public CheckedFuture<Void, TransactionCommitFailedException> execute(EventType eventType, IntentStateTransaction transaction) {
        StateMachineUtils.waitTransactionFinish();
        final Intent.State newState = getNextState(eventType);
        final IntentStateTransactionBuilder newTransactionBuilder = new IntentStateTransactionBuilder(transaction);
        newTransactionBuilder.setCurrentState(newState.getName());
        newTransactionBuilder.setReceivedEvent(eventType.name());
        int deployAttempts = transaction.getDeployAttempts();
        if (deployAttempts > 0) {
            newTransactionBuilder.setDeployAttempts((short)--deployAttempts);
        }
        return engineService.changeTransactionState(newTransactionBuilder.build());
    }

    private Intent.State getNextState(EventType eventType) {
        Intent.State result;
        switch (eventType) {
            case INTENT_ADDED_WITH_SUCCESS:
                result = Intent.State.DEPLOYED;
                break;
            case INTENT_ADDED_ERROR:
                result = Intent.State.DEPLOYFAILED;
                break;
            default:
                result = Intent.State.DEPLOYFAILED;
                break;
        }
        return result;
    }
}
