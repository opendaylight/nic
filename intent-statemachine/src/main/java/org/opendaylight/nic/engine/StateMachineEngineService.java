/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine;


import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;

/**
 * Service to handle state changes
 */
public interface StateMachineEngineService {

    /**
     * Execute State Machine
     * @param transaction
     */
    void execute(IntentStateTransaction transaction);
    /**
     * Change current state to a given state
     * @param currentState
     */
    void changeState(Intent.State currentState);
    /**
     * Push transaction to MDSAL based on a new state
     * @param newState
     */
    void changeTransactionState(String newState);
    /**
     * Push transaction to MDSAL
     */
    void pushTransaction(IntentStateTransaction transaction);
}
