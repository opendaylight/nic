/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.EndpointDiscovered;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.NicNotification;

public class EndpointDiscoveryNotificationSubscriberImpl implements IEventListener<NicNotification>{
    @Override
    public void handleEvent(NicNotification event) {
        if (EndpointDiscovered.class.isInstance(event)) {
            //TODO: Mapping service magic here
        }
    }
}
