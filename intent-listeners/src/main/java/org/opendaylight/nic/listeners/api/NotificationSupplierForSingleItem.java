/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.api;

import org.opendaylight.nic.utils.EventType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public interface NotificationSupplierForSingleItem<O extends DataObject,
        N extends NicNotification> extends NotificationSupplierDefinition<O> {

    N dataChangedNotification(O object, InstanceIdentifier<O> patch);

    Class<?> getImplClass();

    EventType getEventType();
}
