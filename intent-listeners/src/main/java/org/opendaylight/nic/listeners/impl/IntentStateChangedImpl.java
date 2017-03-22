/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.IntentStateChanged;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.IntentStateTransactions;

import java.sql.Timestamp;
import java.util.Calendar;

public class IntentStateChangedImpl implements IntentStateChanged {

    private IntentStateTransactions intentStateTransaction;
    private final Timestamp timestamp;

    public IntentStateChangedImpl(final IntentStateTransactions intentStateTransaction) {
        this.intentStateTransaction = intentStateTransaction;
        this.timestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
    }
    @Override
    public Timestamp getTimeStamp() {
        return timestamp;
    }

    @Override
    public IntentStateTransactions getIntentStateTransaction() {
        return intentStateTransaction;
    }
}
