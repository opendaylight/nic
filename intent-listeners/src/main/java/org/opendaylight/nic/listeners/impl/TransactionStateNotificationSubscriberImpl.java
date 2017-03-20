/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.common.transaction.api.IntentCommonService;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.IntentStateChanged;
import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.IntentStateTransactions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TransactionStateNotificationSubscriberImpl implements IEventListener<NicNotification> {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionStateNotificationSubscriberImpl.class);
    private IntentCommonService intentCommonService;

    private TransactionStateNotificationSubscriberImpl() {}

    public TransactionStateNotificationSubscriberImpl(final IntentCommonService intentCommonService) {
        this.intentCommonService = intentCommonService;
    }

    @Override
    public void handleEvent(NicNotification event) {
        final IntentStateChanged stateChanged = (IntentStateChanged) event;
        final IntentStateTransactions transactions = stateChanged.getIntentStateTransaction();
        //TODO: Call CommonService
        LOG.info("\n#### Transactions event comes to here: {}", transactions.toString());
        final List<IntentStateTransaction> stateTransactions = transactions.getIntentStateTransaction();
        final IntentStateTransaction transaction = stateTransactions.iterator().next();
        intentCommonService.resolveAndApply(transaction.getIntentId());
    }
}
