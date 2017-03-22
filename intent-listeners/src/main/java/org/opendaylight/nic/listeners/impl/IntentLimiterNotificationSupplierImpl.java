/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.listeners.api.*;
import org.opendaylight.nic.utils.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.IntentsLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IntentLimiterNotificationSupplierImpl extends
    AbstractNotificationSupplierItemRoot<IntentLimiter, IntentLimiterAdded, IntentLimiterRemoved, IntentLimiterUpdated>
implements IEventService {

    private static final InstanceIdentifier<IntentLimiter> INTENT_LIMITER_IDD =
            InstanceIdentifier.builder(IntentsLimiter.class)
                    .child(IntentLimiter.class)
                    .build();
    /**
     * Default constructor for all Root Item NicNotification Supplier implementation
     *
     * @param db            - DataBroker for DataChangeEvent registration
     */
    public IntentLimiterNotificationSupplierImpl(DataBroker db) {
        super(db, IntentLimiter.class, LogicalDatastoreType.CONFIGURATION);
        serviceRegistry.setEventTypeService(this,
                EventType.INTENT_LIMITER_ADDED,
                EventType.INTENT_LIMITER_REMOVED,
                EventType.INTENT_LIMITER_UPDATED);
    }

    @Override
    public void addEventListener(IEventListener<?> listener) {
        serviceRegistry.registerEventListener(this, listener);
    }

    @Override
    public void removeEventListener(IEventListener<?> listener) {
        serviceRegistry.unregisterEventListener(this, listener);
    }

    @Override
    public InstanceIdentifier<IntentLimiter> getWildCardPath() {
        return INTENT_LIMITER_IDD;
    }

    @Override
    public IntentLimiterAdded createNotification(IntentLimiter object, InstanceIdentifier<IntentLimiter> path) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(path != null);
        return new IntentLimiterAddedImpl(object);
    }

    @Override
    public IntentLimiterRemoved deleteNotification(IntentLimiter object, InstanceIdentifier<IntentLimiter> path) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(path != null);
        return new IntentLimiterRemovedImpl(object);
    }

    @Override
    public IntentLimiterUpdated updateNotification(IntentLimiter object, InstanceIdentifier<IntentLimiter> path) {
        Preconditions.checkArgument(object != null);
        return new IntentLimiterUpdatedImpl(object);
    }

    @Override
    public EventType getCreateEventType() {
        return EventType.INTENT_LIMITER_ADDED;
    }

    @Override
    public EventType getDeleteEventType() {
        return EventType.INTENT_LIMITER_REMOVED;
    }

    @Override
    public EventType getUpdateEventType() {
        return EventType.INTENT_LIMITER_UPDATED;
    }

    @Override
    public Class<?> getCreateImplClass() {
        return IntentLimiterAddedImpl.class;
    }

    @Override
    public Class<?> getDeleteImplClass() {
        return IntentLimiterRemovedImpl.class;
    }

    @Override
    public Class<?> getUpdateImplClass() {
        return IntentLimiterUpdatedImpl.class;
    }
}