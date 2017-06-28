/*
 * Copyright (c) 2017 Serro LLC .  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.api;

/**
 * Intent Listener service
 */
public interface ListenerProviderService extends AutoCloseable {

    /**
     * Start Intent Listener services
     */
    void start();

    /**
     * Stop Intent Listener services
     */
    void stop();
}
