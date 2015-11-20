/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.IntentAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

import java.sql.Timestamp;
import java.util.Date;

public class IntentAddedImpl implements IntentAdded {
    private Intent intent;
    private final Timestamp timeStamp;

    public IntentAddedImpl(Intent intent) {
        this.intent = intent;
        Date date= new java.util.Date();
        timeStamp = new Timestamp(date.getTime());
    }

    @Override
    public Intent getIntent() {
        return intent;
    }

    @Override
    public Timestamp getTimeStamp() {
        return timeStamp;
    }
}
