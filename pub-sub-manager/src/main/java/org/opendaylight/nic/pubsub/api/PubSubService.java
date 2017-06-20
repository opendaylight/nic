/*
 * Copyright (c) 2017 Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.pubsub.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.IntentLimiter;

/**
 * Created by yrineu on 25/05/17.
 */
public interface PubSubService extends AutoCloseable {

    void start();

    void notifyIntentCreated(final IntentLimiter intentLimiter);
}
