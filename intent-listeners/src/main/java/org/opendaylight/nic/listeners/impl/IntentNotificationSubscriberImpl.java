/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.common.transaction.api.IntentCommonService;
import org.opendaylight.nic.engine.IntentStateMachineExecutorService;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.IntentAdded;
import org.opendaylight.nic.listeners.api.IntentRemoved;
import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.utils.EventType;
import org.opendaylight.nic.utils.FlowAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntentNotificationSubscriberImpl implements IEventListener<NicNotification> {

    private static final Logger LOG = LoggerFactory.getLogger(IntentNotificationSubscriberImpl.class);
    private OFRendererFlowService flowService;
    private IntentCommonService intentCommonService;
    private IntentStateMachineExecutorService stateMachineExecutorService;

    public IntentNotificationSubscriberImpl(OFRendererFlowService flowService) {
        this.flowService = flowService;
    }

    public IntentNotificationSubscriberImpl(final IntentCommonService intentCommonService,
                                            final IntentStateMachineExecutorService stateMachineExecutorService) {
        this.intentCommonService = intentCommonService;
        this.stateMachineExecutorService = stateMachineExecutorService;
    }

    @Override
    public void handleEvent(NicNotification event) {
        if (IntentAdded.class.isInstance(event)) {
            IntentAdded addedEvent = (IntentAdded) event;
            final String intentIdStr = addedEvent.getIntent().getId().toString();
            intentCommonService.resolveAndApply(addedEvent.getIntent());
            stateMachineExecutorService.createTransaction(intentIdStr, EventType.INTENT_ADDED);
        }
        if (IntentRemoved.class.isInstance(event)) {
            IntentRemoved deleteEvent = (IntentRemoved) event;
            final String intentIdStr = deleteEvent.getIntent().getId().toString();
            intentCommonService.resolveAndRemove(deleteEvent.getIntent());
            stateMachineExecutorService.removeTransactions(intentIdStr, EventType.INTENT_REMOVED);
        }
    }
}
