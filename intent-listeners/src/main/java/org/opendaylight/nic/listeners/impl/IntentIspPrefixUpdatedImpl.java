/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.IntentIspPrefixUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.isp.prefix.rev170615.IntentIspPrefix;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by yrineu on 15/06/17.
 */
public class IntentIspPrefixUpdatedImpl implements IntentIspPrefixUpdated {

    private IntentIspPrefix intentIspPrefix;
    private Timestamp timestamp;

    public IntentIspPrefixUpdatedImpl(final IntentIspPrefix intentIspPrefix) {
        this.intentIspPrefix = intentIspPrefix;
        Date date = new Date();
        this.timestamp = new Timestamp(date.getTime());
    }

    @Override
    public IntentIspPrefix getIntent() {
        return intentIspPrefix;
    }

    @Override
    public Timestamp getTimeStamp() {
        return timestamp;
    }
}
