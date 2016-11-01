/*
 * Copyright (c) 2016 Open Networking Foundation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import java.sql.Timestamp;
import java.util.Date;
import org.opendaylight.nic.listeners.api.IntentNBIUpdated;
import org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.IntentDefinition;

public class IntentNBIUpdatedImpl implements IntentNBIUpdated {
    private IntentDefinition intent;
    private final Timestamp timeStamp;

    public IntentNBIUpdatedImpl(final IntentDefinition intent) {
        this.intent = intent;
        Date date= new Date();
        timeStamp = new Timestamp(date.getTime());
    }

    @Override
    public IntentDefinition getIntent() {
        return intent;
    }

    @Override
    public Timestamp getTimeStamp() {
        return timeStamp;
    }
}
