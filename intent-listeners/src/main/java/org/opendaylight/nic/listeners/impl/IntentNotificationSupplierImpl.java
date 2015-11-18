/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.IEventService;
import org.opendaylight.nic.listeners.api.IntentAdded;
import org.opendaylight.nic.listeners.api.IntentRemoved;
import org.opendaylight.nic.listeners.api.IntentUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Implementation define a contract between {@link Intent} data object
 * and {@link IntentAdded} and {@link IntentRemoved} notifications.
 */
public class IntentNotificationSupplierImpl  extends
        AbstractNotificationSupplierItemRoot<Intent, IntentAdded, IntentRemoved, IntentUpdated>
        implements IEventService {

    private static final InstanceIdentifier<Intent> INTENT_IID =
            InstanceIdentifier.builder(Intents.class)
                    .child(Intent.class)
                    .build();

    private static EventServiceRegistry serviceRegistry = EventServiceRegistry.getInstance();
    /**
     * Constructor register supplier as DataChangeLister and create wildCarded InstanceIdentifier.
     *
     * @param db                   - {@link DataBroker}
     */
    public IntentNotificationSupplierImpl(final DataBroker db) {
        super(db, Intent.class, LogicalDatastoreType.CONFIGURATION);
        serviceRegistry.setEventTypeService(this, EventType.INTENT_ADDED, EventType.INTENT_REMOVED);
    }

    @Override
    public InstanceIdentifier<Intent> getWildCardPath() {
        return INTENT_IID;
    }

    @Override
    public IntentAdded createNotification(final Intent object, final InstanceIdentifier<Intent> ii) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(ii != null);
        return new IntentAddedImpl(object);

    }

    @Override
    public IntentRemoved deleteNotification(final Intent object, final InstanceIdentifier<Intent> path) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(path != null);
        return new IntentRemovedImpl(object);
    }

    @Override
    public IntentUpdated updateNotification(Intent object, InstanceIdentifier<Intent> path) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(path != null);
        return new IntentUpdateImpl(object);
    }

    @Override
    public void addEventListener(IEventListener listener) {
        serviceRegistry.registerEventListener(this, listener);
    }

    @Override
    public void removeEventListener(IEventListener listener) {
        serviceRegistry.unregisterEventListener(this, listener);
    }

    @Override
    public EventType getCreateEventType() {
        return EventType.INTENT_ADDED;
    }

    @Override
    public EventType getDeleteEventType() {
        return EventType.INTENT_REMOVED;
    }

    @Override
    public EventType getUpdateEventType() {
        return EventType.INTENT_UPDATE;
    }

    @Override
    public Class getCreateImplClass() {
        return IntentAddedImpl.class;
    }

    @Override
    public Class getDeleteImplClass() {
        return IntentRemovedImpl.class;
    }

    @Override
    public Class getUpdateImplClass() {
        return IntentUpdateImpl.class;
    }
}

