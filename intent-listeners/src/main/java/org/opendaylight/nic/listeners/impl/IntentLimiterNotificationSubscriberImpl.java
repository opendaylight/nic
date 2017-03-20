/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.common.transaction.api.IntentCommonService;
import org.opendaylight.nic.engine.IntentStateMachineExecutorService;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.IntentLimiterAdded;
import org.opendaylight.nic.listeners.api.IntentLimiterRemoved;
import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.nic.utils.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntentLimiterNotificationSubscriberImpl implements IEventListener<NicNotification> {

    private static final Logger LOG = LoggerFactory.getLogger(IntentLimiterNotificationSubscriberImpl.class);
    private IntentCommonService intentCommonService;
    private IntentStateMachineExecutorService stateMachineExecutorService;

    public IntentLimiterNotificationSubscriberImpl(IntentCommonService intentCommonService,
                                                   IntentStateMachineExecutorService stateMachineExecutorService) {
        this.intentCommonService = intentCommonService;
        this.stateMachineExecutorService = stateMachineExecutorService;
    }

    @Override
    public void handleEvent(NicNotification event) {
        if (IntentLimiterAdded.class.isInstance(event)) {
            IntentLimiterAdded addedEvent = (IntentLimiterAdded) event;
            final String intentId = addedEvent.getIntent().getId().toString();
//            stateMachineExecutorService.createTransaction(intentId, EventType.INTENT_LIMITER_ADDED);
            intentCommonService.resolveAndApply(addedEvent.getIntent());
        }

        if (IntentLimiterRemoved.class.isInstance(event)) {
            IntentLimiterRemoved removedEvent = (IntentLimiterRemoved) event;
            final String intentId = removedEvent.getIntent().getId().toString();
//            stateMachineExecutorService.createTransaction(intentId, EventType.INTENT_LIMITER_REMOVED);
            intentCommonService.resolveAndRemove(removedEvent.getIntent());
        }
    }
}
