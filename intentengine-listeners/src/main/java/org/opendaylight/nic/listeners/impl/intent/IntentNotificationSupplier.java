/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl.intent;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.IEventService;
import org.opendaylight.nic.listeners.api.intent.IntentAddedNotification;
import org.opendaylight.nic.listeners.api.intent.IntentRemovedNotification;
import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.nic.listeners.api.intent.IntentRemoved;
import org.opendaylight.nic.listeners.api.intent.IntentAdded;
import org.opendaylight.nic.listeners.impl.AbstractNotificationSupplierItemRoot;
import org.opendaylight.nic.listeners.impl.EventServiceRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yrineu on 05/10/15.
 */
public class IntentNotificationSupplier extends AbstractNotificationSupplierItemRoot<Intent,
        IntentAddedNotification, IntentRemovedNotification>
        implements IEventService {

    private ListenerRegistration<DataChangeListener> intentListener = null;

    private EventServiceRegistry eventServiceRegistry;

    private static final Logger LOG = LoggerFactory.getLogger(IntentNotificationSupplier.class);

    /**
     * Default constructor for all Root Item NicNotification Supplier implementation
     *
     * @param db    - DataBroker for DataChangeEvent registration
     * @param clazz - Statistics NicNotification Class
     */
    public IntentNotificationSupplier(DataBroker db, Class<Intent> clazz) {
        super(db, clazz, LogicalDatastoreType.CONFIGURATION);
        LOG.info("Starting Intent listener");

        eventServiceRegistry = EventServiceRegistry.getInstance();
        eventServiceRegistry.setEventTypeService(this, EventType.INTENT_ADDED, EventType.INTENT_REMOVED);
    }

    @Override
    public IntentAddedNotification createNotification(Intent object, InstanceIdentifier<Intent> path) {
        return new IntentAdded();
    }

    @Override
    public IntentRemovedNotification deleteNotification(InstanceIdentifier<Intent> path) {
        return new IntentRemoved();
    }

    @Override
    public InstanceIdentifier<Intent> getWildCardPath() {
        return InstanceIdentifier.builder(Intents.class).child(Intent.class).build();
    }

    @Override
    public void addEventListener(IEventListener listener) {
        eventServiceRegistry.registerEventListener(this, listener);
    }

    @Override
    public void removeEventListener(IEventListener listener) {
        eventServiceRegistry.unregisterEventListener(this, listener);
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
    public Class getCreateImplClass() {
        return IntentAdded.class;
    }

    @Override
    public Class getDeleteImplClass() {
        return IntentRemoved.class;
    }
}