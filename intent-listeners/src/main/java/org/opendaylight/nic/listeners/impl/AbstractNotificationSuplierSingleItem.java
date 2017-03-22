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
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.listeners.api.EventRegistryService;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.nic.listeners.api.NotificationSupplierForSingleItem;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

abstract class AbstractNotificationSuplierSingleItem<O extends DataObject,
        N extends NicNotification>
        extends AbstractNotificationSupplierBase<O>
        implements NotificationSupplierForSingleItem<O, N> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractNotificationSuplierSingleItem.class);
    protected EventRegistryService eventRegistryService = null;
    /**
     * Default constructor for all NicNotification Supplier implementation
     *
     * @param db            - {@link DataBroker}
     * @param clazz         - API contract class extended {@link DataObject}
     * @param datastoreType - Either Operational or Configuration data store
     */
    public AbstractNotificationSuplierSingleItem(final DataBroker db,
                                                 final Class<O> clazz,
                                                 final LogicalDatastoreType datastoreType) {
        super(db, clazz, datastoreType);
        final BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        final ServiceReference<?> serviceReference = context.getServiceReference(EventRegistryService.class);
        eventRegistryService = (EventRegistryService) context.getService(serviceReference);
    }

    @Override
    public void onDataChanged(final AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> dataChanged) {
        Preconditions.checkNotNull(dataChanged);
        Map<InstanceIdentifier<?>, DataObject> changeAsMap =
                (!dataChanged.getCreatedData().isEmpty() ? dataChanged.getCreatedData() : dataChanged.getUpdatedData());
        if (dataChanged != null && !(changeAsMap.isEmpty())) {
            for (final Map.Entry<InstanceIdentifier<?>, DataObject> createDataObj : changeAsMap.entrySet()) {
                if (clazz.isAssignableFrom(createDataObj.getKey().getTargetType())) {
                    final InstanceIdentifier<O> ii = createDataObj.getKey().firstIdentifierOf(clazz);
                    final N notif = dataChangedNotification((O) createDataObj.getValue(), ii);
                    if (notif != null) {
                        if (getImplClass().isInstance(notif)) {
                            Set<IEventListener<?>> eventListeners =
                                    eventRegistryService.getEventListeners(getEventType());
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
