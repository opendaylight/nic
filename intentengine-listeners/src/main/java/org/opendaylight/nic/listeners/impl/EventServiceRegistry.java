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
import org.opendaylight.nic.listeners.api.IEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class EventServiceRegistry {

    private static EventServiceRegistry serviceRegistry = null;
    private HashMap<IEventService, Set<IEventListener>> eventRegistry = new HashMap<>();
    private HashMap<EventType, IEventService> typeRegistry = new HashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(TopologyEventPublisher.class);

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

    public void registerEventListener(IEventService service, IEventListener listener) {
        if (!eventRegistry.containsKey(service)) {
            HashSet<IEventListener> eventListeners = new HashSet<>();
            eventListeners.add(listener);
            eventRegistry.put(service,eventListeners);
        } else {
            Set<IEventListener> eventListeners = eventRegistry.get(service);
            eventListeners.add(listener);
            eventRegistry.put(service, eventListeners);
        }
    }

    public void unregisterEventListener(IEventService service, IEventListener listener) {
        if (eventRegistry.containsKey(service)) {
            Set<IEventListener> eventListeners = eventRegistry.get(service);
            eventListeners.remove(listener);
            if (eventListeners.isEmpty()) {
                eventRegistry.remove(service);
            }
        } else {
            LOG.info("No Event publisher service is registered for listener: ", listener);
        }
    }

    public void setEventTypeService(IEventService service, EventType ... supportedTypes) {
        for (int index = 0; index < supportedTypes.length; index++) {
            typeRegistry.put(supportedTypes[index], service);
        }
    }

    public IEventService getEventService(EventType eventType) {
        return typeRegistry.get(eventType);
    }

    public void notifyEvent(EventType eventType) {
        //Notify appropriate listener
        IEventService service = getEventService(eventType);
        Set<IEventListener> eventListenerSet = eventRegistry.get(service);
        for (IEventListener listener: eventListenerSet) {
            listener.handleEvent(eventType);
        }
    }
}
