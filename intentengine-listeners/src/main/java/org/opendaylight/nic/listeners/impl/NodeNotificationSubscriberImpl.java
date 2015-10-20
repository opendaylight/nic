/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.of.renderer.api.FlowAction;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.nic.listeners.api.NodeDeleted;
import org.opendaylight.nic.listeners.api.NodeUp;

class NodeNotificationSubscriberImpl implements IEventListener<NicNotification> {

    private  OFRendererFlowService flowService;

    public NodeNotificationSubscriberImpl(OFRendererFlowService flowService) {
        this.flowService = flowService;
    }

    @Override
    public void handleEvent(NicNotification event) {
        if (NodeUp.class.isInstance(event)) {
            NodeUp nodeUp = (NodeUp) event;
            flowService.pushARPFlow(nodeUp.getNodeId(), FlowAction.ADD_FLOW);
        }
        if (NodeDeleted.class.isInstance(event)) {
            //TODO: Since node is deleted flow no longer exists on switch as there is no switch
        }
    }
}