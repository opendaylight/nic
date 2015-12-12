/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.EventRegistryService;
import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.IEventService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
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
public class EventRegistryServiceImpl implements EventRegistryService{

    private Map<IEventService, Set<IEventListener<?>>> eventRegistry = new HashMap<>();
    private Map<EventType, IEventService> typeRegistry = new HashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(EventRegistryServiceImpl.class);
    protected ServiceRegistration<EventRegistryService> nicEventServiceRegistration;

    public EventRegistryServiceImpl() {
        // Register this service with karaf
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        nicEventServiceRegistration = context.registerService(EventRegistryService.class, this, null);
    }

    @Override
    public void registerEventListener(IEventService service, IEventListener<?> listener) {
        if (!eventRegistry.containsKey(service)) {
            Set<IEventListener<?>> eventListeners = new HashSet<>();
            eventListeners.add(listener);
            eventRegistry.put(service, eventListeners);
        } else {
            Set<IEventListener<?>> eventListeners = eventRegistry.get(service);
            eventListeners.add(listener);
            eventRegistry.put(service, eventListeners);
        }
    }

    @Override
    public void registerEventListener(EventType eventType, IEventListener<?> listener) {
        IEventService service = getEventService(eventType);
        if (service != null) {
            if (!eventRegistry.containsKey(service)) {
                Set<IEventListener<?>> eventListeners = new HashSet<>();
                eventListeners.add(listener);
                eventRegistry.put(service, eventListeners);
            } else {
                Set<IEventListener<?>> eventListeners = eventRegistry.get(service);
                eventListeners.add(listener);
                eventRegistry.put(service, eventListeners);
            }
        }
        else {
            LOG.error("No event supplier registered for Event type {}", eventType);
        }
    }

    @Override
    public void unregisterEventListener(IEventService service, IEventListener<?> listener) {
        if (eventRegistry.containsKey(service)) {
            Set<IEventListener<?>> eventListeners = eventRegistry.get(service);
            eventListeners.remove(listener);
            if (eventListeners.isEmpty()) {
                eventRegistry.remove(service);
            }
        } else {
            LOG.info("No Event publisher service is registered for listener: ", listener);
        }
    }

    @Override
    public void setEventTypeService(IEventService service, EventType ... supportedTypes) {
        for (int index = 0; index < supportedTypes.length; index++) {
            typeRegistry.put(supportedTypes[index], service);
        }
    }

    @Override
    public IEventService getEventService(EventType eventType) {
        return typeRegistry.get(eventType);
    }

    @Override
    public Set<IEventListener<?>> getEventListeners(EventType eventType) {
        IEventService eventService = getEventService(eventType);
        return eventRegistry.get(eventService);
    }

    protected Map<IEventService, Set<IEventListener<?>>> getEventRegistry() {
        return eventRegistry;
    }

    protected Map<EventType, IEventService> getTypeRegistry() {
        return typeRegistry;
    }
}