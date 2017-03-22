/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.engine.utils;

import org.opendaylight.nic.utils.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransactionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent.State.DEPLOYING;

/**
 * Created by yrineu on 31/03/17.
 */
public class StateMachineUtils {
    private static final Logger LOG = LoggerFactory.getLogger(StateMachineUtils.class);

    public static final String INITIAL_STATE = Intent.State.UNDEPLOYED.getName();
    public static final short MAX_ATTEMPTS = 3;

    public static IntentStateTransaction decrementAttempts(final EventType eventType,
                                                           final IntentStateTransaction transaction,
                                                           final String currentState) {
        final IntentStateTransactionBuilder transactionBuilder = new IntentStateTransactionBuilder(transaction);

        transactionBuilder.setCurrentState(currentState);
        transactionBuilder.setReceivedEvent(eventType.name());
        return decrementRemainingAttempts(transactionBuilder);
    }

    public static IntentStateTransaction buildNewTransactionBy(final IntentStateTransaction currentTransaction,
                                                               final Intent.State newState,
                                                               final EventType event) {
        final IntentStateTransactionBuilder transactionBuilder = new IntentStateTransactionBuilder(currentTransaction);
        transactionBuilder.setReceivedEvent(event.name());
        transactionBuilder.setCurrentState(newState.getName());
        return transactionBuilder.build();
    }

    private static IntentStateTransaction decrementRemainingAttempts(final IntentStateTransactionBuilder transactionBuilder) {
        final EventType eventType = EventType.valueOf(transactionBuilder.getReceivedEvent());
        short deployAttempts = transactionBuilder.getDeployAttempts();
        short undeployAttempts = transactionBuilder.getUndeployAttempts();
        switch (eventType) {
            case INTENT_BEING_ADDED:
            case INTENT_ADDED_ERROR:
                if (deployAttempts > 0) {
                    transactionBuilder.setDeployAttempts(--deployAttempts);
                } else {
                    transactionBuilder.setDeployAttempts(deployAttempts);
                }
                break;
            case INTENT_BEING_REMOVED:
            case INTENT_REMOVE_ERROR:
                if (undeployAttempts > 0) {
                    transactionBuilder.setUndeployAttempts(--undeployAttempts);
                } else {
                    transactionBuilder.setUndeployAttempts(undeployAttempts);
                }
                break;
        }
        return transactionBuilder.build();
    }

    public static void waitTransactionFinish() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }
    }

    public static boolean isReadyToProceed(final IntentStateTransaction transaction) {
        final Intent.State currentState = Intent.State.valueOf(transaction.getCurrentState());
        boolean result;
        switch (currentState) {
            case DEPLOYING:
            case UNDEPLOYING:
            case DISABLING:
                result = true;
                break;
            default:
                result = false;
                break;
        }
        return result;
    }

    public synchronized static IntentStateTransaction decrementAttemptsIfNeed(final Intent.State newState,
                                                           final IntentStateTransaction transaction,
                                                           final EventType eventType) {
        final IntentStateTransaction resultedTransaction;
        final Intent.State currentState = Intent.State.valueOf(transaction.getCurrentState());
        final boolean currentStateIsDeploying = isDeploying(currentState);
        final boolean newStateIsDeploying = isDeploying(newState);
        if (currentStateIsDeploying || newStateIsDeploying) {
            resultedTransaction = StateMachineUtils.decrementAttempts(eventType, transaction, newState.getName());
        } else {
            resultedTransaction = StateMachineUtils.buildNewTransactionBy(transaction, newState, eventType);
        }
        return resultedTransaction;
    }

    private static boolean isDeploying(final Intent.State state) {
        return Intent.State.DEPLOYING.equals(state);
    }
}
