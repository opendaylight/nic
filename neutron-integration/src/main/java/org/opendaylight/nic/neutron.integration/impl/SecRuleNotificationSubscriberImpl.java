/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.neutron.integration.impl;

import org.opendaylight.nic.listeners.api.*;

class SecRuleNotificationSubscriberImpl implements IEventListener<NicNotification> {

    @Override
    public void handleEvent(NicNotification event) {
        if (SecurityRuleAdded.class.isInstance(event)) {
            //TODO: Translate Security rules into intents
        }
        else if (SecurityRuleDeleted.class.isInstance(event)) {
            //TODO
        }
        else if(SecurityRuleUpdated.class.isInstance(event)) {
            //TODO:
        }
    }
}