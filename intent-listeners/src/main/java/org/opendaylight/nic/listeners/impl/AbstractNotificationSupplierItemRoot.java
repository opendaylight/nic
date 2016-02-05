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
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.listeners.api.EventRegistryService;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.nic.listeners.api.NotificationSupplierForItemRoot;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Class is package protected abstract implementation for all Root Items
 * NicNotification Suppliers
 *
 * @param <O> - data tree item Object
 * @param <C> - Create notification
 * @param <D> - Delete notification
 * @param <U> - Update notification
 */
abstract class AbstractNotificationSupplierItemRoot<O extends DataObject,
        C extends NicNotification,
        D extends NicNotification,
        U extends NicNotification>
        extends AbstractNotificationSupplierBase<O>
        implements NotificationSupplierForItemRoot<O, C, D, U> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractNotificationSupplierItemRoot.class);
    protected EventRegistryService serviceRegistry = null;

    /**
     * Default constructor for all Root Item NicNotification Supplier implementation
     *
     * @param db - DataBroker for DataChangeEvent registration
     * @param clazz - Statistics NicNotification Class
     */
    public AbstractNotificationSupplierItemRoot(final DataBroker db, final Class<O> clazz,
                                                LogicalDatastoreType datastoreType) {
        super(db, clazz, datastoreType);
        // Retrieve reference for Event Registry service
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<?> serviceReference = context.
                getServiceReference(EventRegistryService.class);
        serviceRegistry = (EventRegistryService) context.
                getService(serviceReference);
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        Preconditions.checkArgument(change != null, "ChangeEvent can not be null!");

        created(change.getCreatedData());
        update(change.getUpdatedData());
        deleted(change);
    }

    //TODO: Refactor to avoid duplicated code
    protected void created(Map<InstanceIdentifier<?>, DataObject> change) {
        if (change != null && ! (change.isEmpty())) {
            for (final Entry<InstanceIdentifier<?>, DataObject> createDataObj : change.entrySet()) {
                if (clazz.isAssignableFrom(createDataObj.getKey().getTargetType())) {
                    final InstanceIdentifier<O> ii = createDataObj.getKey().firstIdentifierOf(clazz);
                    final C notif = createNotification((O) createDataObj.getValue(), ii);
                    if (notif != null) {
                        LOG.trace("NicNotification created");
                        if (getCreateImplClass().isInstance(notif)) {
                            Set<IEventListener<?>> eventListeners =
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

    protected void update(Map<InstanceIdentifier<?>, DataObject> updatedData) {
        if (updatedData != null && ! (updatedData.isEmpty())) {
            for (final Entry<InstanceIdentifier<?>, DataObject> updatedObject : updatedData.entrySet()) {
                if (clazz.isAssignableFrom(updatedObject.getKey().getTargetType())) {
                    final InstanceIdentifier<O> ii = updatedObject.getKey().firstIdentifierOf(clazz);
                    final U notif = updateNotification((O) updatedObject.getValue(), ii);
                    if (notif != null) {
                        LOG.trace("NicNotification update");
                        if (getUpdateImplClass().isInstance(notif)) {
                            Set<IEventListener<?>> eventListeners =
                                    serviceRegistry.getEventListeners(getUpdateEventType());
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

    protected void deleted(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        if (change.getRemovedPaths() != null && !(change.getRemovedPaths().isEmpty())) {
            for (final InstanceIdentifier<?> deleteDataPath : change.getRemovedPaths()) {
                if (clazz.isAssignableFrom(deleteDataPath.getTargetType())) {
                    Map<InstanceIdentifier<?>, DataObject> original = change.getOriginalData();
                    final D notif = deleteNotification( (O) original.get(deleteDataPath),
                            deleteDataPath.firstIdentifierOf(clazz));
                    if (notif != null) {
                        LOG.trace("NicNotification deleted");
                        if (getDeleteImplClass().isInstance(notif)) {
                            Set<IEventListener<?>> eventListeners =
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
