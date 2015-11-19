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

//TODO: This class must be a Akka actor to be executed on MD-SAL
public class StateTransaction {

    private Intent intent;
    private boolean executing;
    private Date startTime;
    private StateMachineEngineService machineEngineService;
    private EventType eventType;

    public StateTransaction(Intent intent, EventType eventType) {
        this.intent = intent;
        this.eventType = eventType;
        startTime = Calendar.getInstance().getTime();
        machineEngineService = new StateMachineEngineImpl(intent);
    }

    public Uuid getIntentId() {
        return intent.getId();
    }

    public boolean inExecution() {
        return executing;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void execute() {
        executing = true;
        machineEngineService.execute();
    }
}
