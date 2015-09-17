/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.nic.listeners.api.IEventListener;

import java.util.HashSet;
import java.util.Set;

public final class EventServiceRegistry {

    private static EventServiceRegistry serviceRegistry = null;
    //TODO: Maybe a hashmap from EventTypes to registered listeners for the type?
    private Set<IEventListener> eventListeners = new HashSet<>();
//    private HashMap<IEventService, IEventListener> eventRegistry = new HashMap<>();

    private EventServiceRegistry() {

    }

    //Registry is a Singleton
    //TODO: Not thread safe for now
    public static EventServiceRegistry getInstance() {
        if (serviceRegistry == null) {
            serviceRegistry = new EventServiceRegistry();
        }
        return serviceRegistry;
    }

    //TODO: Register the corresponding publisher for this listener?
    //TODO: registerEventListener(IEEventService, IEventListener)
    public void registerEventListener(IEventListener listener) {
        eventListeners.add(listener);
    }

    public void unregisterEventListener(IEventListener listener) {
        eventListeners.remove(listener);
    }

    public void notifyEvent(EventType type) {
        //TODO: Notify appropriate listener
    }
}
