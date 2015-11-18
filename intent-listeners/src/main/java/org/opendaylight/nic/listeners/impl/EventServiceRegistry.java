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
import java.util.Map;
import java.util.Set;

/**
 * Events Registry that stores mappings from the event providers
 * to their supported types and Listeners
 *
 */
public final class EventServiceRegistry {

    private static volatile EventServiceRegistry serviceRegistry = null;
    private Map<IEventService, Set<IEventListener>> eventRegistry = new HashMap<>();
    private Map<EventType, IEventService> typeRegistry = new HashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(EventServiceRegistry.class);

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
            Set<IEventListener> eventListeners = new HashSet<>();
            eventListeners.add(listener);
            eventRegistry.put(service, eventListeners);
        } else {
            Set<IEventListener> eventListeners = eventRegistry.get(service);
            eventListeners.add(listener);
            eventRegistry.put(service, eventListeners);
        }
    }

    public void registerEventListener(EventType eventType, IEventListener listener) {
        IEventService service = getEventService(eventType);
        if (service != null) {
            if (!eventRegistry.containsKey(service)) {
                Set<IEventListener> eventListeners = new HashSet<>();
                eventListeners.add(listener);
                eventRegistry.put(service, eventListeners);
            } else {
                Set<IEventListener> eventListeners = eventRegistry.get(service);
                eventListeners.add(listener);
                eventRegistry.put(service, eventListeners);
            }
        }
        else {
            LOG.error("No event supplier registered for Event type {}", eventType);
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

    public Set<IEventListener> getEventListeners(EventType eventType) {
        IEventService eventService = getEventService(eventType);
        return eventRegistry.get(eventService);
    }
}
