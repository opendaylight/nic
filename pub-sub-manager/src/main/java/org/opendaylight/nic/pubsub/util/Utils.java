/*
 * Copyright (c) 2017 Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.pubsub.util;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.IntentsLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.external.notification.rev170526.ExternalNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.external.notification.rev170526.ExternalNotifications;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.external.notifier.rev170601.ExternalNotifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.external.notifier.rev170601.ExternalNotifiers;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.external.subscriber.rev170531.ExternalSubscriber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.external.subscriber.rev170531.ExternalSubscribers;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.IntentStateTransactions;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * Created by yrineu on 30/05/17.
 */
public class Utils {
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    public static final InstanceIdentifier<ExternalSubscribers> EXTERNAL_SUBSCRIBERS_IDENTIFIER =
            InstanceIdentifier.builder(ExternalSubscribers.class).build();
    public static final InstanceIdentifier<ExternalNotifiers> NOTIFIERS_INSTANCE_IDENTIFIER =
            InstanceIdentifier.builder(ExternalNotifiers.class).build();
    public static final InstanceIdentifier<ExternalNotifications> EXTERNAL_NOTIFICATION_IDENTIFIER =
            InstanceIdentifier.builder(ExternalNotifications.class).build();
    public static final InstanceIdentifier<IntentsLimiter> INTENTS_LIMITER_IDENTIFIER =
            InstanceIdentifier.builder(IntentsLimiter.class).build();
    public static final InstanceIdentifier<IntentStateTransactions> INTENT_STATE_TRANSACTION_IDENTIFIER =
            InstanceIdentifier.builder(IntentStateTransactions.class).build();
    public static final DataTreeIdentifier DATA_TREE_IDENTIFIER = new DataTreeIdentifier(
            LogicalDatastoreType.CONFIGURATION,
            EXTERNAL_NOTIFICATION_IDENTIFIER);

    public static final String CONTROLLER_IP = "ovsdb.controller.address";
    public static final String NIC_NOTIFICATION_URL = "/restconf/config/external-notification:external-notifications";

    public static IntentLimiter getIntentLimiter(final DataBroker dataBroker,
                                                 final String id) {
        IntentLimiter intentLimiter;
        final Uuid uuid = Uuid.getDefaultInstance(id);
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
        final InstanceIdentifier<IntentLimiter> LIMITER_ID = InstanceIdentifier
                .create(IntentsLimiter.class).child(IntentLimiter.class, new IntentLimiterKey(uuid));
        try {
            final Optional<IntentLimiter> result = transaction.read(
                    LogicalDatastoreType.CONFIGURATION,
                    LIMITER_ID).checkedGet();
            intentLimiter = result.get();
        } catch (ReadFailedException e) {
            throw new NoSuchElementException(e.getMessage());
        }
        return intentLimiter;
    }

    public static List<ExternalNotification> getExternalNotifications(final DataBroker dataBroker) {
        final List<ExternalNotification> result = Lists.newArrayList();
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<ExternalNotifications>, ReadFailedException> future
                = transaction.read(LogicalDatastoreType.CONFIGURATION, Utils.EXTERNAL_NOTIFICATION_IDENTIFIER);
        try {
            Optional<ExternalNotifications> externalNotifications = future.checkedGet();
            externalNotifications.asSet().forEach(notification -> result.addAll(notification.getExternalNotification()));
        } catch (ReadFailedException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public static List<ExternalSubscriber> getExternalSubscribers(final DataBroker dataBroker) {
        final List<ExternalSubscriber> result = Lists.newArrayList();
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();

        try {
            Optional<ExternalSubscribers> externalSubscribers = transaction.read(
                    LogicalDatastoreType.CONFIGURATION,
                    EXTERNAL_SUBSCRIBERS_IDENTIFIER).checkedGet();
            if (externalSubscribers.isPresent()) {
                    result.addAll(externalSubscribers.get().getExternalSubscriber());
            }
        } catch (ReadFailedException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public static List<ExternalNotifier> getExternalNotifiers(final DataBroker dataBroker) {
        final List<ExternalNotifier> result = Lists.newArrayList();
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();

        try {
            Optional<ExternalNotifiers> externalNotifiers = transaction.read(
                    LogicalDatastoreType.CONFIGURATION,
                    NOTIFIERS_INSTANCE_IDENTIFIER).checkedGet();
            if (externalNotifiers != null) {
                result.addAll(externalNotifiers.get().getExternalNotifier());
            }
        } catch (ReadFailedException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public static IntentLimiter retrieveIntentLimiterBy(final DataBroker dataBroker,
                                                        final String ipAddress) {
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
        CheckedFuture<Optional<IntentsLimiter>, ReadFailedException> checkedFuture =
                transaction.read(LogicalDatastoreType.CONFIGURATION, Utils.INTENTS_LIMITER_IDENTIFIER);
        final List<IntentLimiter> results = Lists.newArrayList();
        try {
            final Optional<IntentsLimiter> intents = checkedFuture.checkedGet();
            intents.asSet().forEach(intentsLimiterConsumer ->
                    intentsLimiterConsumer.getIntentLimiter().forEach(intentLimiter -> {
                        if (intentLimiter.getSourceIp().getValue().equals(ipAddress)) {
                            results.add(intentLimiter);
                        }
                    }));
        } catch (ReadFailedException e) {
            LOG.error(e.getMessage());
        }

        final IntentLimiter toReturn;
        if (results.iterator().hasNext()) {
            toReturn = results.iterator().next();
        } else {
            throw new NoSuchElementException();
        }
        return toReturn;
    }


    public static String getControllerIp() {
        String result = null;
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            if (enumeration.hasMoreElements()) {
                NetworkInterface element = enumeration.nextElement();
                final InetAddress address = element.getInetAddresses().nextElement();
                result = address.getHostAddress();
            }
        } catch (SocketException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }
}
