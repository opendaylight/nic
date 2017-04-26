/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.api;

import org.opendaylight.nic.utils.EventType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Notification service for a single item
 * @param <O> the instance that extends {@link DataObject}
 * @param <N> the {@link NicNotification}
 */
public interface NotificationSupplierForSingleItem<O extends DataObject,
        N extends NicNotification> extends NotificationSupplierDefinition<O> {

    /**
     * The notification for a given {@link DataObject}
     * @param object the {@link DataObject}
     * @param patch the {@link InstanceIdentifier} related to {@link DataObject}
     * @return a {@link NicNotification}
     */
    N dataChangedNotification(O object, InstanceIdentifier<O> patch);

    /**
     * Get the {@link NicNotification} impl class
     * @return
     */
    Class<?> getImplClass();

    /**
     * Retrieve the {@link EventType}
     * @return a {@link EventType}
     */
    EventType getEventType();
}
