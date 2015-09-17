/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.IntentAdded;
import org.opendaylight.nic.listeners.api.IntentRemoved;
import org.opendaylight.nic.listeners.api.NicNotification;

public class IntentNotificationSubscriberImpl implements IEventListener<NicNotification> {
    @Override
    public void handleEvent(NicNotification event) {
        if (IntentAdded.class.isInstance(event)) {
            //TODO: Push flow using OFRenderer service
        }
        if (IntentRemoved.class.isInstance(event)) {
            //TODO: Delete flow using OFRenderer service
        }
    }
}
