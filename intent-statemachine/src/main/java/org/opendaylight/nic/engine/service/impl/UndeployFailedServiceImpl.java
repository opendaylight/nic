/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.service.impl;

import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.nic.engine.api.StateMachine;
import org.opendaylight.nic.engine.service.api.UndeployFailedService;
import org.opendaylight.nic.engine.utils.StateMachineUtils;
import org.opendaylight.nic.utils.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent.State.*;

public class UndeployFailedServiceImpl implements UndeployFailedService {
    private static final Logger LOG = LoggerFactory.getLogger(UndeployFailedServiceImpl.class);

    private static StateMachine engineService;
    private static UndeployFailedService undeployFailedService;

    private UndeployFailedServiceImpl(final StateMachine engineService) {
        this.engineService = engineService;
    }

    public static UndeployFailedService getInstance(StateMachine engineService) {
        if(undeployFailedService == null) {
            undeployFailedService = new UndeployFailedServiceImpl(engineService);
        }
        return undeployFailedService;
    }

    @Override
    public CheckedFuture<Void, TransactionCommitFailedException> execute(final EventType eventType,
                                                                         final IntentStateTransaction transaction) {
        StateMachineUtils.waitTransactionFinish();
        final IntentStateTransaction transactionVerified;
        final Intent.State newState = getNextState(eventType);
        transactionVerified = StateMachineUtils.buildNewTransactionBy(transaction,
                newState,
                eventType);
        return engineService.changeTransactionState(transactionVerified);
    }

    private Intent.State getNextState(final EventType eventType) {
        Intent.State result;

        switch (eventType) {
            case INTENT_REMOVE_ATTEMPT:
                result = UNDEPLOYING;
                break;
            case INTENT_BEING_DISABLED:
                result = DISABLING;
                break;
            case INTENT_REMOVE_RETRY_WITH_MAX_ATTEMPTS:
                result = UNDEPLOYFAILED;
                break;
            default:
                result = DISABLING;
                break;
        }
        return result;
    }
}