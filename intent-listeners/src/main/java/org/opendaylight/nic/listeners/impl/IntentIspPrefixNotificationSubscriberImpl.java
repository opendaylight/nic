/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.common.transaction.api.IntentCommonService;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.IntentIspPrefixAdded;
import org.opendaylight.nic.listeners.api.IntentIspPrefixRemoved;
import org.opendaylight.nic.listeners.api.NicNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yrineu on 15/06/17.
 */
public class IntentIspPrefixNotificationSubscriberImpl implements IEventListener<NicNotification> {

    private static final Logger LOG = LoggerFactory.getLogger(IntentIspPrefixNotificationSubscriberImpl.class);
    private IntentCommonService intentCommonService;

    public IntentIspPrefixNotificationSubscriberImpl(final IntentCommonService intentCommonService) {
        this.intentCommonService = intentCommonService;
    }

    @Override
    public void handleEvent(NicNotification event) {
        if (IntentIspPrefixAdded.class.isInstance(event)) {
            IntentIspPrefixAdded ispAdded = (IntentIspPrefixAdded) event;
            LOG.info("\n### Intent ISP added: {}", ispAdded.getIntent().getIspName());
            intentCommonService.resolveAndApply(ispAdded.getIntent());
        }

        if (IntentIspPrefixRemoved.class.isInstance(event)) {
            IntentIspPrefixRemoved ispRemoved = (IntentIspPrefixRemoved) event;
            LOG.info("\n### Intent ISP removed: {}", ispRemoved.getIntent().getIspName());
        }
    }
}
