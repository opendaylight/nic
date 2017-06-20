/*
 * Copyright (c) 2017 Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.pubsub.manager;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.common.transaction.api.IntentCommonService;
import org.opendaylight.nic.pubsub.api.PubSubService;
import org.opendaylight.nic.pubsub.service.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.IntentLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

/**
 * Created by yrineu on 26/05/17.
 */
public class PubSubServiceImpl implements PubSubService {
    private static final Logger LOG = LoggerFactory.getLogger(PubSubServiceImpl.class);

    private final DataBroker dataBroker;
    private final IntentCommonService intentCommonService;

    TransactionListenerService transactionListenerService;
    ExternalNotificationService extenalExternalNotificationService;

    public PubSubServiceImpl(final DataBroker dataBroker,
                             final IntentCommonService intentCommonService) {
        this.dataBroker = dataBroker;
        this.intentCommonService = intentCommonService;
    }

    @Override
    public void start() {
        LOG.info("\nPub/Sub manager initialized.");
        final ActionExecutorService executorService = new ActionExecutorServiceImpl(dataBroker);
        this.transactionListenerService = new TransactionListenerImpl(dataBroker, executorService);
        transactionListenerService.start();
        this.extenalExternalNotificationService = new ExternalNotificationServiceImpl(dataBroker, intentCommonService);
        extenalExternalNotificationService.start();
    }

    @Override
    public void notifyIntentCreated(IntentLimiter intentLimiter) {
        LOG.info("\n### Subscribing for external events: Device IP: {}", intentLimiter.getSourceIp());
        try {
            extenalExternalNotificationService.subscribeForExternalAlerts(intentLimiter.getSourceIp().getValue());
        } catch (UnknownHostException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void close() throws Exception {
        transactionListenerService.close();
        extenalExternalNotificationService.close();
    }
}
