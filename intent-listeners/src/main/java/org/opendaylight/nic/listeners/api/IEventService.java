/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.api;

/**
 * Interface for implementing a service that publishes events
 */
public interface IEventService {
    /**
     * Adds a listener
     *
     * @param listener listener to be added.
     */
    void addEventListener(IEventListener listener);

    /**
     * Removes a listener for application events.
     *
     * @param listener listener to be removed
     */
    void removeEventListener(IEventListener listener);

}
