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
import org.opendaylight.nic.engine.service.DeployedService;
import org.opendaylight.nic.engine.utils.StateMachineUtils;
import org.opendaylight.nic.utils.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yrineu on 10/04/17.
 */
public class DeployedServiceImpl implements DeployedService {
    private static final Logger LOG = LoggerFactory.getLogger(DeployedServiceImpl.class);

    private static StateMachine engineService;
    private static DeployedService deployedService;

    private DeployedServiceImpl(final StateMachine engineService) {
        this.engineService = engineService;
    }

    public static DeployedService getInstance(final StateMachine engineService) {
        if (deployedService == null) {
            deployedService = new DeployedServiceImpl(engineService);
        }
        return deployedService;
    }

    @Override
    public CheckedFuture<Void, TransactionCommitFailedException> execute(EventType eventType, IntentStateTransaction transaction) {
        StateMachineUtils.waitTransactionFinish();
        final Intent.State newState = getNextState(eventType);
        final IntentStateTransaction newTransaction = StateMachineUtils.buildNewTransactionBy(transaction, newState, eventType);
        return engineService.changeTransactionState(newTransaction);
    }

    private Intent.State getNextState(final EventType eventType) {
        Intent.State result;

        switch (eventType) {
            case INTENT_DISABLE:
            case INTENT_BEING_DISABLED:
            case INTENT_BEING_REMOVED:
                result = Intent.State.UNDEPLOYING;
                break;
            default:
                result = Intent.State.DEPLOYED;
                break;
        }
        return result;
    }
}
