/*
 * Copyright (c) 2017 Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.engine.api;

import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.nic.utils.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;

/**
 * Service to handle Intent state change
 */
public interface StateMachine {

    /**
     * Start State Machine services
     */
    void start();
    /**
     * Execute State Machine
     * @param transaction
     * @return the {@link CheckedFuture}
     */
    CheckedFuture<Void, TransactionCommitFailedException> execute(IntentStateTransaction transaction);
    /**
     * Change current state to a given state
     * @param transaction
     * @return the {@link CheckedFuture}
     */
    CheckedFuture<Void, TransactionCommitFailedException> changeState(IntentStateTransaction transaction);
    /**
     * Push transaction to MDSAL based on a new state
     * @param transaction
     * @return {@link CheckedFuture}
     */
    CheckedFuture<Void, TransactionCommitFailedException> changeTransactionState(IntentStateTransaction transaction);
    /**
     * Push transaction to MDSAL
     * @param transaction the IntentStateTransaction
     * @return {@link CheckedFuture}
     */
    CheckedFuture<Void, TransactionCommitFailedException> pushTransaction(IntentStateTransaction transaction);

    /**
     * Retrieve transaction for a given ID.
     * @param id The Intent ID as {@link String}
     * @return the IntentStateTransaction
     */
    IntentStateTransaction retrieveTransaction(String id);

    /**
     * Verify if can execute action based on remaining attempts
     * @param id the Intent ID as {@link String}
     * @param eventType the {@link EventType}
     * @return a {@link Boolean} value
     */
    boolean canExecute(String id, EventType eventType);

    /**
     * Stop State Machine services
     */
    void stop();
}
