/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.IntentLimiterRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;

import java.sql.Timestamp;
import java.util.Date;

public class IntentLimiterRemovedImpl implements IntentLimiterRemoved {

    private IntentLimiter intentLimiter;
    private final Timestamp timestamp;

    public IntentLimiterRemovedImpl(IntentLimiter intentLimiter) {
        this.intentLimiter = intentLimiter;
        Date date = new Date();
        this.timestamp = new Timestamp(date.getTime());
    }

    @Override
    public Timestamp getTimeStamp() {
        return timestamp;
    }

    @Override
    public IntentLimiter getIntent() {
        return intentLimiter;
    }
}
