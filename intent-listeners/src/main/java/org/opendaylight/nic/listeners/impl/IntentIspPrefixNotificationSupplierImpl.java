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
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.isp.prefix.rev170615.IntentIspPrefixes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.isp.prefix.rev170615.intent.isp.prefixes.IntentIspPrefix;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by yrineu on 15/06/17.
 */
public class IntentIspPrefixNotificationSupplierImpl extends
        AbstractNotificationSupplierItemRoot<IntentIspPrefix, IntentIspPrefixAdded, IntentIspPrefixRemoved, IntentIspPrefixUpdated>
        implements IEventService {

    private static final InstanceIdentifier<IntentIspPrefix> INTENT_ISP_IDENTIFIER =
            InstanceIdentifier.builder(IntentIspPrefixes.class)
                    .child(IntentIspPrefix.class)
                    .build();

    /**
     * Default constructor for all Root Item NicNotification Supplier implementation
     *
     * @param db - DataBroker for DataChangeEvent registration
     */
    public IntentIspPrefixNotificationSupplierImpl(DataBroker db) {
        super(db, IntentIspPrefix.class, LogicalDatastoreType.CONFIGURATION);
        serviceRegistry.setEventTypeService(this,
                EventType.INTENT_ISP_ADDED,
                EventType.INTENT_ISP_REMOVED,
                EventType.INTENT_ISP_UPDATED);
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
    public InstanceIdentifier<IntentIspPrefix> getWildCardPath() {
        return INTENT_ISP_IDENTIFIER;
    }

    @Override
    public IntentIspPrefixAdded createNotification(IntentIspPrefix object, InstanceIdentifier<IntentIspPrefix> path) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(path != null);
        return new IntentIspPrefixAddedImpl(object);
    }

    @Override
    public IntentIspPrefixRemoved deleteNotification(IntentIspPrefix object, InstanceIdentifier<IntentIspPrefix> path) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(path != null);
        return new IntentIspIspRemovedImpl(object);
    }

    @Override
    public IntentIspPrefixUpdated updateNotification(IntentIspPrefix object, InstanceIdentifier<IntentIspPrefix> path) {
        Preconditions.checkArgument(object != null);
        return new IntentIspPrefixUpdatedImpl(object);
    }

    @Override
    public EventType getCreateEventType() {
        return EventType.INTENT_ISP_ADDED;
    }

    @Override
    public EventType getDeleteEventType() {
        return EventType.INTENT_ISP_REMOVED;
    }

    @Override
    public EventType getUpdateEventType() {
        return EventType.INTENT_ISP_UPDATED;
    }

    @Override
    public Class<?> getCreateImplClass() {
        return IntentIspPrefixAddedImpl.class;
    }

    @Override
    public Class<?> getDeleteImplClass() {
        return IntentIspIspRemovedImpl.class;
    }

    @Override
    public Class<?> getUpdateImplClass() {
        return IntentIspPrefixUpdatedImpl.class;
    }
}
