/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.api;

/**
 * This service is responsible for Intent Life Cycle Management
 */
public interface IntentCommonService {

    /**
     * Init all Intent processes
     */
    void start();

    /**
     * Extract and apply IntentLimiter. That Intent is used to
     * create Intents with meters
     * @param intent as an {@link Object}
     */
    void resolveAndApply(final Object intent);

    /**
     * Extract and remove Intent limiter
     * @param intent as {@link Object}
     */
    void resolveAndRemove(final Object intent);

    /**
     * Stop all Intent processes
     */
    void stop();
}
