/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.api;

import java.util.Set;

/**
 * Created by saket on 11/24/15.
 */
public interface EventRegistryService {

    /**
     * Register listener with the corresponding event provider service
     * @param service - {@link IEventListener}
     * @param listener - {@link IEventListener}
     */
    void registerEventListener(IEventService service, IEventListener<?> listener);

    /**
     * Register listener with the corresponding eventType
     * @param eventType - {@link EventType}
     * @param listener - {@link IEventListener}
     */
    void registerEventListener(EventType eventType, IEventListener<?> listener);

    /**
     * Unregister listener from the event provider service
     * @param service - {@link IEventService}
     * @param listener - {@link IEventListener}
     */
    void unregisterEventListener(IEventService service, IEventListener<?> listener);

    /**
     * Associate the service with its published event types
     * @param service - {@link IEventService}
     * @param supportedTypes - {@link EventType}
     */
    void setEventTypeService(IEventService service, EventType ... supportedTypes);

    /**
     * Retrieve the event provider service associated with the eventType
     * @param eventType - {@link EventType}
     * @return - {@link IEventService}
     */
    IEventService getEventService(EventType eventType);

    /**
     * Retrieve all the registered event listeners associated with eventType
     * @param eventType - {@link EventType}
     * @return - return the set of associated listeners
     */
    Set<IEventListener<?>> getEventListeners(EventType eventType);

}
