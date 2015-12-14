/*
 * Copyright (c) 2015 Inocybe inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.nic.listeners.api.TopologyLinkDeleted;
import org.opendaylight.nic.listeners.api.TopologyLinkUp;
import org.opendaylight.nic.of.renderer.api.OFRendererGraphService;

public class TopologyLinkNotificationSubscriberImpl implements IEventListener<NicNotification> {

    private OFRendererGraphService graphService;

    public TopologyLinkNotificationSubscriberImpl(OFRendererGraphService graphService) {
        this.graphService = graphService;
    }

    @Override
    public void handleEvent(NicNotification event) {
        if (TopologyLinkUp.class.isInstance(event)) {
            // TODO transaction to retrieve the Links
            if (graphService.getGraph() != null) {
                // temp if
            }
        }
        if (TopologyLinkDeleted.class.isInstance(event)) {
            if (graphService.getGraph() != null) {
                // temp if
            }
        }
    }

}
