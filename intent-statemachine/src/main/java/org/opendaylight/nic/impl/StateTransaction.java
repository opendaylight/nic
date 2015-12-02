/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.impl;

import org.opendaylight.nic.engine.StateMachineEngineService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

import java.util.Calendar;
import java.util.Date;

public class StateTransaction {

    private Intent intent;
    private boolean executing;
    private Date startTime;
    private StateMachineEngineService machineEngineService;
    private ReceivedEvent receivedEvent;

    public StateTransaction(Intent intent, ReceivedEvent receivedEvent) {
        this.intent = intent;
        this.receivedEvent = receivedEvent;
        startTime = Calendar.getInstance().getTime();
        machineEngineService = new StateMachineEngineImpl(intent);
    }

    public Uuid getIntentId() {
        return intent.getId();
    }

    public boolean inExecution() {
        return executing;
    }

    public ReceivedEvent getReceivedEvent() {
        return receivedEvent;
    }

    public void execute() {
        executing = true;
        machineEngineService.execute();
    }

    public void stop() {
        executing = false;
        //TODO: Stop current transaction
    }
}
