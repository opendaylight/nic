/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine;


import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.transaction.rev151203.ism.transactions.IntentTransaction;

/**
 * Service to handle state changes
 */
public interface StateMachineEngineService {

    /**
     * Execute State Machine by a transaction builder
     */
    void execute();

    /**
     * Execute State Machine by a existing transaction
     * @param intentTransaction
     */
    void execute(IntentTransaction intentTransaction);
    /**
     * Change current state to a given state
     * @param currentState
     */
    void changeState(Intent.State currentState);

    /**
     * Update transaction on MD-SAL with current state
     * @param currentState
     */
    void updateTransaction(Intent.State currentState);

    /**
     * Retrieve IntentId
     * @return IntentId
     */
    Uuid getIntentId();
}
