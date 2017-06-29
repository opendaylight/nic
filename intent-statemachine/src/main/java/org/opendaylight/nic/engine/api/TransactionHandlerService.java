/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.engine.api;

import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;

import java.util.NoSuchElementException;

/**
 * Transaction service to manage {@link IntentStateTransaction}
 */
public interface TransactionHandlerService {

    /**
     * Send transaction to MDSAL
     * @param transaction the IntentStateTransaction
     * @return the {@link CheckedFuture}
     */
    CheckedFuture<Void, TransactionCommitFailedException> sendTransaction(final IntentStateTransaction transaction);

    /**
     * Remove a given transaction from MDSAL
     * @param transaction the IntentStateTransaction
     */
    void destroyTransaction(final IntentStateTransaction transaction);

    /**
     * Store a IntentStateTransaction when the state is changed
     * @param transaction
     * @return the {@link CheckedFuture}
     */
    CheckedFuture<Void, TransactionCommitFailedException> storeStateChange(final IntentStateTransaction transaction);

    /**
     *
     * @param id the IntentStateTransaction ID as {@link String}
     * @return the {@link CheckedFuture}
     * @throws NoSuchElementException it can throws an exception in case of {@link IntentStateTransaction} is
     * missing
     */
    IntentStateTransaction retrieveTransaction(final String id) throws NoSuchElementException;
}
