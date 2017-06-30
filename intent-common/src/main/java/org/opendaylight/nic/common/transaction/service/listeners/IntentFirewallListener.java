/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.listeners;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.common.transaction.api.IntentCommonService;
import org.opendaylight.nic.common.transaction.utils.InstanceIdentifierUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created by yrineu on 30/06/17.
 */
public class IntentFirewallListener extends AbstractListener<Intents>
        implements IntentTreeChangesListener<Intents> {
    private static final Logger LOG = LoggerFactory.getLogger(IntentFirewallListener.class);

    private final DataBroker dataBroker;
    private final IntentCommonService intentCommonService;
    private ListenerRegistration<IntentFirewallListener> registration;

    public IntentFirewallListener(final DataBroker dataBroker,
                                  final IntentCommonService intentCommonService) {
        super();
        this.dataBroker = dataBroker;
        this.intentCommonService = intentCommonService;
    }

    @Override
    public void start() {
        LOG.info("\nIntent Firewall listener initiated");
        final DataTreeIdentifier<Intents> dataTreeIdentifier = new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, InstanceIdentifierUtils.INTENTS_FIREWALL_IDENTIFIER);
        registration = dataBroker.registerDataTreeChangeListener(dataTreeIdentifier, this);
    }


    @Override
    void handleIntentCreated(Intents intents) {
        intents.getIntent().forEach(intent -> intentCommonService.resolveAndApply(intent));
    }

    @Override
    void handleIntentUpdated(Intents intents) {
        //TODO: Implement update for Intent Firewall
    }

    @Override
    void handleIntentRemoved(Intents intents) {
        intents.getIntent().forEach(intent -> intentCommonService.resolveAndRemove(intent));
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<Intents>> collection) {
        super.handleIntentTreeEvent(collection);
    }

    @Override
    public void stop() {
        registration.close();
    }
}
