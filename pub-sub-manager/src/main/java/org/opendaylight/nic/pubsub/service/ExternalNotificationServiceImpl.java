/*
 * Copyright (c) 2017 Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.pubsub.service;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.nic.common.transaction.api.IntentCommonService;
import org.opendaylight.nic.pubsub.util.Utils;
import org.opendaylight.nic.pubsub.webservice.WebServiceSubscriberOperation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.external.notification.rev170526.ExternalNotifications;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yrineu on 26/05/17.
 */
public class ExternalNotificationServiceImpl implements ExternalNotificationService {
    private static final Logger LOG = LoggerFactory.getLogger(ExternalNotificationServiceImpl.class);

    private ListenerRegistration<DataTreeChangeListener> dataChangeListenerRegistration;

    private final DataBroker dataBroker;
    private final IntentCommonService intentCommonService;
    private ExecutorService executorService;

    public ExternalNotificationServiceImpl(
            final DataBroker dataBroker,
            final IntentCommonService intentCommonService) {
        Preconditions.checkNotNull(dataBroker);
        Preconditions.checkNotNull(intentCommonService);

        this.dataBroker = dataBroker;
        this.intentCommonService = intentCommonService;
    }

    public void start() {
        this.executorService = Executors.newFixedThreadPool(50);
        dataChangeListenerRegistration = dataBroker.registerDataTreeChangeListener(
                Utils.DATA_TREE_IDENTIFIER,
                this);
        LOG.info("\nExternal notification listener service initialized");
    }

    @Override
    public void subscribeForExternalAlerts(String ipAddress) throws UnknownHostException {
        final String controllerIp = Utils.getControllerIp();
        final String urlForNotification = controllerIp + Utils.NIC_NOTIFICATION_URL;
        Utils.getExternalNotifiers(dataBroker).forEach(externalService -> {
            LOG.info("\n#### External URL: {} - NIC URL: {}", externalService.getExternalUrl(), urlForNotification);
            executorService.execute(new WebServiceSubscriberOperation(
                    externalService.getExternalUrl(),
                    urlForNotification,
                    ipAddress));
        });
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<ExternalNotifications>> collection) {
        collection.iterator().forEachRemaining((DataTreeModification<ExternalNotifications> consumer) -> {
            final ExternalNotifications externalNotification = consumer.getRootNode().getDataAfter();
            Preconditions.checkNotNull(externalNotification);
            externalNotification.getExternalNotification().forEach(external -> {
                try {
                    final String deviceIp = external.getDeviceIp();
                    LOG.info("\n#### Received ExternalNotification for IP: {}", external.getDeviceIp());
                    final IntentLimiter intentLimiter = Utils.retrieveIntentLimiterBy(dataBroker, deviceIp);
                    intentCommonService.resolveAndApply(intentLimiter);
                } catch (NoSuchElementException e) {
                    LOG.error(e.getMessage());
                }
            });
        });
    }

    @Override
    public void close() {
        dataChangeListenerRegistration.close();
        executorService.shutdown();
    }
}
