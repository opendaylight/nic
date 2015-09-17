/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.api;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Supplier Root Item contracts definition for every NicNotifications. All root items
 * are described by two notifications. NicNotification for Create and Delete.
 * So interface has to contain two methods for relevant NicNotification.
 *
 * @param <O> - data tree item Object
 * @param <C> - Create notification
 * @param <D> - Delete notification
 */
public interface NotificationSupplierForItemRoot<O extends DataObject,
                                             C extends NicNotification,
                                             D extends NicNotification>
                extends NotificationSupplierDefinition<O> {

    /**
     * Method produces relevant addItem kind of {@link NicNotification} from
     * data tree item identified by {@link InstanceIdentifier} path.
     * 
     * @param object - Data Tree Item object
     * @param path - Identifier of Data Tree Item
     * @return {@link NicNotification} - relevant API contract NicNotification
     */
    C createNotification(O object, InstanceIdentifier<O> path);

    /**
     * Method produces relevant deleteItem kind of {@link NicNotification} from
     * path {@link InstanceIdentifier} to deleted item.
     * 
     * @param path - Identifier of Data Tree Item
     * @return {@link NicNotification} - relevant API contract NicNotification
     */
    D deleteNotification(O object, InstanceIdentifier<O> path);
}

