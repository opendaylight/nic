/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.nic.listeners.api.NotificationSupplierForItemRoot;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Class is package protected abstract implementation for all Root Items
 * NicNotification Suppliers
 *
 * @param <O> - data tree item Object
 * @param <C> - Create notification
 * @param <D> - Delete notification
 */
//TODO: Extend to support update notif
abstract class AbstractNotificationSupplierItemRoot<O extends DataObject,
        C extends NicNotification,
        D extends NicNotification>
        extends AbstractNotificationSupplierBase<O>
        implements NotificationSupplierForItemRoot<O, C, D> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractNotificationSupplierItemRoot.class);
    private static EventServiceRegistry serviceRegistry = EventServiceRegistry.getInstance();

    /**
     * Default constructor for all Root Item NicNotification Supplier implementation
     *
     * @param db - DataBroker for DataChangeEvent registration
     * @param clazz - Statistics NicNotification Class
     */
    public AbstractNotificationSupplierItemRoot(final DataBroker db, final Class<O> clazz,
                                                LogicalDatastoreType datastoreType) {
        super(db, clazz, datastoreType);
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        Preconditions.checkArgument(change != null, "ChangeEvent can not be null!");

        created(change.getCreatedData());
        deleted(change);
    }

    private void created(Map<InstanceIdentifier<?>, DataObject> change) {
        if (change != null && ! (change.isEmpty())) {
            for (final Entry<InstanceIdentifier<?>, DataObject> createDataObj : change.entrySet()) {
                if (clazz.isAssignableFrom(createDataObj.getKey().getTargetType())) {
                    final InstanceIdentifier<O> ii = createDataObj.getKey().firstIdentifierOf(clazz);
                    final C notif = createNotification((O) createDataObj.getValue(), ii);
                    if (notif != null) {
                        LOG.info("NicNotification created");
                        if (getCreateImplClass().isInstance(notif)) {
                            Set<IEventListener> eventListeners =
                                    serviceRegistry.getEventListeners(getCreateEventType());
                            if (eventListeners != null) {
                                for (IEventListener listener : eventListeners) {
                                    listener.handleEvent(notif);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void deleted(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        if (change.getRemovedPaths() != null && !(change.getRemovedPaths().isEmpty())) {
            for (final InstanceIdentifier<?> deleteDataPath : change.getRemovedPaths()) {
                if (clazz.isAssignableFrom(deleteDataPath.getTargetType())) {
                    Map<InstanceIdentifier<?>, DataObject> original = change.getOriginalData();
                    final D notif = deleteNotification( (O) original.get(deleteDataPath),
                            deleteDataPath.firstIdentifierOf(clazz));
                    if (notif != null) {
                        LOG.info("NicNotification deleted");
                        if (getDeleteImplClass().isInstance(notif)) {
                            Set<IEventListener> eventListeners =
                                    serviceRegistry.getEventListeners(getDeleteEventType());
                            if (eventListeners != null) {
                                for (IEventListener listener : eventListeners) {
                                    listener.handleEvent(notif);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}