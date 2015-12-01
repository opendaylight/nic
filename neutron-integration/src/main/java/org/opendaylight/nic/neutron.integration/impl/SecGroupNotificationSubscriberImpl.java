/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.neutron.integration.impl;

import org.opendaylight.nic.listeners.api.*;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.utils.FlowAction;

class SecGroupNotificationSubscriberImpl implements IEventListener<NicNotification> {

    @Override
    public void handleEvent(NicNotification event) {
        if (SecurityGroupAdded.class.isInstance(event)) {
            //TODO: Translate Security group into intents
        }
        else if (SecurityGroupDeleted.class.isInstance(event)) {
            //TODO
        }
        else if(SecurityGroupUpdated.class.isInstance(event)) {
            //TODO:
        }
    }
}