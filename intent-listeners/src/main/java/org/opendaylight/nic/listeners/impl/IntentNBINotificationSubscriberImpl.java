/*
 * Copyright (c) 2016 Open Networking Foundation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.IntentNBIAdded;
import org.opendaylight.nic.listeners.api.IntentNBIRemoved;
import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;


public class IntentNBINotificationSubscriberImpl implements IEventListener<NicNotification> {

    private OFRendererFlowService flowService;

    public IntentNBINotificationSubscriberImpl(OFRendererFlowService flowService) {
        this.flowService = flowService;
    }

    @Override
    public void handleEvent(NicNotification event) {
        if (IntentNBIAdded.class.isInstance(event)) {
            IntentNBIAdded addedEvent = (IntentNBIAdded) event;
            //TODO: Abstract pushIntentFlow method to receive the new Intent-NBI
            //flowService.pushIntentFlow(addedEvent.getIntent(), FlowAction.ADD_FLOW);
        }
        if (IntentNBIRemoved.class.isInstance(event)) {
            IntentNBIRemoved deleteEvent = (IntentNBIRemoved) event;
            //TODO: Abstract pushIntentFlow method to receive the new Intent-NBI
            //flowService.pushIntentFlow(deleteEvent.getIntent(), FlowAction.REMOVE_FLOW);
        }
    }
}
