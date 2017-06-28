/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine;

import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.nic.utils.EventType;

/**
 * Service to handle State Machine transactions
 */
public interface IntentStateMachineExecutorService extends AutoCloseable {

    /**
     * Init Intent State Machine executor service
     */
    void start();

    /**
     * Create a new transaction for a given Intent based in a given event
     * @param intentId the Intent ID as {@link String}
     * @param receivedEvent the received event as {@link EventType}
     * @return {@link CheckedFuture}
     */
    CheckedFuture<Void, TransactionCommitFailedException> createTransaction(String intentId, EventType receivedEvent);

    /**
     * Send Intent to the next transaction. Usually this method is called when try to
     * evaluate an renderer action.
     * @param intentId the IntentID as {@link String}
     * @param eventType the {@link EventType}
     * @return {@link CheckedFuture}
     */
    CheckedFuture<Void, TransactionCommitFailedException> goToNextTransaction(String intentId, EventType eventType);

    /**
     * Remove a transaction for a given Intent
     * @param intentId the Intent ID as {@link String}
     * @param receivedEvent the {@link EventType}
     */
    void removeTransactions(String intentId, EventType receivedEvent);

    /**
     * Verify if a give Transaction still have Deploy or Undeploy attempts
     * @param id the Intent ID as {@link String}
     * @param eventType the {@link EventType}
     * @return a {@link Boolean} value
     */
    boolean canEvaluateAttempt(String id, EventType eventType);

    /**
     * Stop State Machine processes.
     */
    void stop();
}
