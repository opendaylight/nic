/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.engine.impl;

import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.nic.engine.StateMachine;
import org.opendaylight.nic.engine.service.UndeployingService;
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
public class UndeployingServiceImpl implements UndeployingService {
    private static final Logger LOG = LoggerFactory.getLogger(UndeployingServiceImpl.class);

    private static StateMachine engineService;
    private static UndeployingService undeployingService;

    private UndeployingServiceImpl(final StateMachine engineService) {
        this.engineService = engineService;
    }

    public static UndeployingService getInstance(final StateMachine engineService) {
        if (undeployingService == null) {
            undeployingService = new UndeployingServiceImpl(engineService);
        }
        return undeployingService;
    }

    @Override
    public CheckedFuture<Void, TransactionCommitFailedException> execute(EventType eventType, IntentStateTransaction transaction) {
        StateMachineUtils.waitTransactionFinish();
        final Intent.State newState = getNextState(eventType);
        final IntentStateTransactionBuilder newTransactionBuilder = new IntentStateTransactionBuilder(transaction);
        newTransactionBuilder.setCurrentState(newState.getName());
        newTransactionBuilder.setReceivedEvent(eventType.name());
        int unDeployAttempts = transaction.getUndeployAttempts();
        if (unDeployAttempts > 0) {
            newTransactionBuilder.setUndeployAttempts((short)--unDeployAttempts);
        }
        return engineService.changeTransactionState(newTransactionBuilder.build());
    }

    private Intent.State getNextState(final EventType eventType) {
        Intent.State result;

        switch (eventType) {
            case INTENT_REMOVED_WITH_SUCCESS:
                result = Intent.State.UNDEPLOYED;
                break;
            case INTENT_REMOVE_ERROR:
                result = Intent.State.UNDEPLOYFAILED;
                break;
            default:
                result = Intent.State.UNDEPLOYFAILED;
                break;
        }
        return result;
    }
}
