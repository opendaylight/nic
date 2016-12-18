/*
 * Copyright (c) 2016 Open Networking Foundation and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.onf.intent.nbi.rev160920.IntentDefinitions;
import org.opendaylight.yang.gen.v1.urn.onf.intent.nbi.rev160920.intent.definitions.IntentDefinition;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Implementation define a contract between {@link IntentDefinition} data object
 * and {@link IntentNBIAdded} and {@link IntentNBIRemoved} notifications.
 */
public class IntentNBINotificationSupplierImpl  extends
        AbstractNotificationSupplierItemRoot<IntentDefinition, IntentNBIAdded, IntentNBIRemoved, IntentNBIUpdated>
        implements IEventService {

    private static final InstanceIdentifier<IntentDefinition> INTENTS_NBI_IID =
            InstanceIdentifier.builder(IntentDefinitions.class)
                    .child(IntentDefinition.class).build();

    /**
     * Constructor register supplier as DataChangeLister and create wildCarded InstanceIdentifier.
     *
     * @param db                   - {@link DataBroker}
     */
    public IntentNBINotificationSupplierImpl(final DataBroker db) {
        super(db, IntentDefinition.class, LogicalDatastoreType.CONFIGURATION);
        serviceRegistry.setEventTypeService(this, EventType.INTENT_NBI_ADDED, EventType.INTENT_NBI_REMOVED, EventType.INTENT_NBI_UPDATE);
    }

    @Override
    public InstanceIdentifier<IntentDefinition> getWildCardPath() {
        return INTENTS_NBI_IID;
    }

    @Override
    public IntentNBIAdded createNotification(final IntentDefinition object, final InstanceIdentifier<IntentDefinition> ii) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(ii != null);
        return new IntentNBIAddedImpl(object);

    }

    @Override
    public IntentNBIRemoved deleteNotification(final IntentDefinition object, final InstanceIdentifier<IntentDefinition> path) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(path != null);
        return new IntentNBIRemovedImpl(object);
    }

    @Override
    public IntentNBIUpdated updateNotification(final IntentDefinition object, final InstanceIdentifier<IntentDefinition> path) {
        Preconditions.checkArgument(object != null);
        return new IntentNBIUpdatedImpl(object);
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
    public EventType getCreateEventType() {
        return EventType.INTENT_NBI_ADDED;
    }

    @Override
    public EventType getDeleteEventType() {
        return EventType.INTENT_NBI_REMOVED;
    }

    @Override
    public EventType getUpdateEventType() {
        return EventType.INTENT_NBI_UPDATE;
    }

    @Override
    public Class<?> getCreateImplClass() {
        return IntentAddedImpl.class;
    }

    @Override
    public Class<?> getDeleteImplClass() {
        return IntentRemovedImpl.class;
    }

    @Override
    public Class<?> getUpdateImplClass() {
        return IntentUpdateImpl.class;
    }
}
