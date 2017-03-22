/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;

/**
 * Event when an {@link IntentLimiter} have removed
 */
public interface IntentLimiterRemoved extends NicNotification {

    /**
     * Retrieve the {@link IntentLimiter} removed
     * @return {@link IntentLimiter}
     */
    IntentLimiter getIntent();
}
