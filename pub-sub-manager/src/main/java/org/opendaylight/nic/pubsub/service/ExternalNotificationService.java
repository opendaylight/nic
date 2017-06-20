/*
 * Copyright (c) 2017 Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.pubsub.service;

import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.external.notification.rev170526.ExternalNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.external.notification.rev170526.ExternalNotifications;

import java.net.UnknownHostException;

/**
 * Created by yrineu on 26/05/17.
 */
public interface ExternalNotificationService extends DataTreeChangeListener<ExternalNotifications> {

    void start();

    void subscribeForExternalAlerts(String ipAddress) throws UnknownHostException;

    void close();
}
