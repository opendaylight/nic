/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.IEventService;
import org.opendaylight.nic.listeners.api.IntentStateChanged;
import org.opendaylight.nic.utils.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.IntentStateTransactions;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class TransactionStateNotificationSuplierImpl
        extends AbstractNotificationSuplierSingleItem<IntentStateTransactions, IntentStateChanged>
        implements IEventService {

    private static final InstanceIdentifier<IntentStateTransactions> INTENT_STATE_TRANSACTION_II =
            InstanceIdentifier.builder(IntentStateTransactions.class).build();

    /**
     * Default constructor for all NicçNotification Supplier implementation
     *
     * @param db - {@link DataBroker}
     */
    public TransactionStateNotificationSuplierImpl(final DataBroker db) {
        super(db, IntentStateTransactions.class, LogicalDatastoreType.CONFIGURATION);
        eventRegistryService.setEventTypeService(this, EventType.INTENT_STATE_TRANSACTION);
    }

    @Override
    public void addEventListener(IEventListener<?> listener) {
        eventRegistryService.registerEventListener(this, listener);
    }

    @Override
    public void removeEventListener(IEventListener<?> listener) {
        eventRegistryService.unregisterEventListener(this, listener);
    }

    @Override
    public IntentStateChanged dataChangedNotification(IntentStateTransactions transactions,
                                                      InstanceIdentifier<IntentStateTransactions> patch) {
        return new IntentStateChangedImpl(transactions);
    }

    @Override
    public Class<?> getImplClass() {
        return IntentStateChangedImpl.class;
    }

    @Override
    public EventType getEventType() {
        return EventType.INTENT_STATE_TRANSACTION;
    }

    @Override
    public InstanceIdentifier<IntentStateTransactions> getWildCardPath() {
        return INTENT_STATE_TRANSACTION_II;
    }
}