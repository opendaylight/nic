/*
 * Copyright (c) 2017 Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.pubsub.service;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.pubsub.util.Utils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.IntentLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.IntentStateTransactions;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yrineu on 25/05/17.
 */
public class TransactionListenerImpl implements TransactionListenerService {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionListenerImpl.class);

    private interface NotificationAction {
        void notifyAction(String value);
    }

    private ListenerRegistration<DataTreeChangeListener> dataChangeListenerRegistration;
    private final DataBroker dataBroker;
    private final ActionExecutorService actionExecutorService;

    private final Map<Intent.State, NotificationAction> notificationActionMap = new ConcurrentHashMap<>();

    public TransactionListenerImpl(final DataBroker dataBroker,
                                   final ActionExecutorService actionExecutorService) {
        Preconditions.checkNotNull(dataBroker);
        this.dataBroker = dataBroker;
        this.actionExecutorService = actionExecutorService;
    }

    @Override
    public void start() {
        final DataTreeIdentifier dataTreeIdentifier = new DataTreeIdentifier(
                LogicalDatastoreType.CONFIGURATION,
                Utils.INTENT_STATE_TRANSACTION_IDENTIFIER);
        dataChangeListenerRegistration = dataBroker.registerDataTreeChangeListener(
                dataTreeIdentifier,
                this);
        populateNotificationActionMap();
        LOG.info("\nTransaction listener service initialized.");
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<IntentStateTransactions>> collection) {
        collection.iterator().forEachRemaining(consumer -> {
            final IntentStateTransactions transactions = consumer.getRootNode().getDataAfter();
            transactions.getIntentStateTransaction().forEach(transaction -> {
                final String state = transaction.getCurrentState();
                final NotificationAction actionService = notificationActionMap.get(Intent.State.valueOf(state));
                if (actionService != null) {
                    final IntentLimiter intentLimiter = Utils.getIntentLimiter(dataBroker, transaction.getIntentId());
                    actionService.notifyAction(intentLimiter.getSourceIp().getValue());
                } else {
                    LOG.info("\n### No action for state {}", state);
                }
            });
        });
    }

    @Override
    public void close() {
        dataChangeListenerRegistration.close();
        actionExecutorService.close();
    }

    //Notification action as a MAP to increase action capabilities.
    private void populateNotificationActionMap() {
        notificationActionMap.put(Intent.State.DEPLOYED, (value) -> actionExecutorService.notifyMitigatedAction(value));
    }
}
