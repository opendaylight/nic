/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.GraphEdgeAdded;
import org.opendaylight.nic.listeners.api.GraphEdgeDeleted;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;

public class GraphEdgeNotificationSubscriberImpl implements IEventListener<NicNotification> {

    private OFRendererFlowService flowService;

    public GraphEdgeNotificationSubscriberImpl(OFRendererFlowService flowService) {
        this.flowService = flowService;
    }
    /**
     * Issues notification about Graph edge event
     *
     * @param event Application event notification
     */
    @Override
    public void handleEvent(NicNotification event) {
        if (GraphEdgeAdded.class.isInstance(event)) {
            GraphEdgeAdded addedEvent = (GraphEdgeAdded) event;
            //TODO: Push flows
//            flowService.pushIntentFlow(addedEvent.getIntent(), FlowAction.ADD_FLOW);
        }
        if (GraphEdgeDeleted.class.isInstance(event)) {
            GraphEdgeDeleted deleteEvent = (GraphEdgeDeleted) event;
            //TODO: Delete installed flows
//            flowService.pushIntentFlow(deleteEvent.getIntent(), FlowAction.REMOVE_FLOW);
        }
    }
}
