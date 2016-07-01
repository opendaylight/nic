/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.statemachine.api.IntentStateMachineExecutorService;
import org.opendaylight.nic.listeners.api.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.nic.transaction.api.EventType;

public class IntentNotificationSubscriberImpl implements IEventListener<NicNotification> {

    private IntentStateMachineExecutorService stateMachineExecutorService;

    public IntentNotificationSubscriberImpl(IntentStateMachineExecutorService stateMachineExecutorService) {
        this.stateMachineExecutorService = stateMachineExecutorService;
    }

    @Override
    public void handleEvent(NicNotification event) {
        if (IntentAdded.class.isInstance(event)) {
            final IntentAdded addedEvent = (IntentAdded) event;
            final Uuid intentId = addedEvent.getIntent().getId();
            stateMachineExecutorService.createTransaction(intentId, EventType.INTENT_ADDED);
            //TODO: Change to use ISM
            //flowService.pushIntentFlow(addedEvent.getIntent(), FlowAction.ADD_FLOW);
        }
        if (IntentRemoved.class.isInstance(event)) {
            IntentRemoved deleteEvent = (IntentRemoved) event;
            //flowService.pushIntentFlow(deleteEvent.getIntent(), FlowAction.REMOVE_FLOW);
        }
    }
}
