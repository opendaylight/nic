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
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.listeners.api.NotificationSupplierDefinition;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Public abstract basic Supplier implementation contains code for a make Supplier instance,
 * registration Supplier like {@link org.opendaylight.controller.md.sal.binding.api.DataChangeListener}
 * and close method. In additional case, it contains help methods for all Supplier implementations.
 *
 * @param <O> - data tree item Object extends {@link DataObject}
 */
public abstract class AbstractNotificationSupplierBase<O extends DataObject> implements
        NotificationSupplierDefinition<O> {

    protected final Class<O> clazz;
    private ListenerRegistration<DataChangeListener> listenerRegistration;

    /**
     * Default constructor for all NicNotification Supplier implementation
     *
     * @param db    - {@link DataBroker}
     * @param clazz - API contract class extended {@link DataObject}
     * @param datastoreType - Either Operational or Configuration data store
     */
    public AbstractNotificationSupplierBase(final DataBroker db, final Class<O> clazz,
                                            LogicalDatastoreType datastoreType) {
        Preconditions.checkArgument(db != null, "DataBroker can not be null!");
        listenerRegistration = db.registerDataChangeListener(datastoreType, getWildCardPath(), this,
                DataChangeScope.BASE);
        this.clazz = clazz;
    }

    @Override
    public void close() throws Exception {
        if (listenerRegistration != null) {
            listenerRegistration.close();
        }
    }
}
