/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import java.util.ArrayList;
import org.opendaylight.nic.listeners.api.IEventListener;

public class EventServiceRegistry implements org.opendaylight.nic.listeners.api.IEventService {

    private ArrayList<IEventListener> eventListeners = new ArrayList<>();

    public void addEventListener(IEventListener listener) {
        eventListeners.add(listener);
    }

    public void removeEventListener(IEventListener listener) {
        eventListeners.remove(listener);
    }
}
