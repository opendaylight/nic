/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.api;

/**
 * Interface for receiving notification about various application events
 */
public interface IEventListener {
    /**
     * Issues notification about an application event.
     * @param eventType Application event type
     */
    void handleEvent(EventType eventType);
}

