/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.engine.service;

import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;

public interface TransactionHandlerService {

    void sendTransaction(final IntentStateTransaction transaction);

    void destroyTransaction(final IntentStateTransaction transaction);

    void storeStateChange(final String id, final String newState, final String event);
}
