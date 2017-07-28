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
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.vlan.rev170724.IntentVlans;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created by yrineu on 25/07/17.
 */
public class IntentVlanListener extends AbstractListener<IntentVlans>
    implements IntentTreeChangesListener<IntentVlans> {
    public static final Logger LOG = LoggerFactory.getLogger(IntentVlanListener.class);

    private DataBroker dataBroker;
    private IntentCommonService intentCommonService;
    private ListenerRegistration registration;

    public IntentVlanListener(final DataBroker dataBroker,
                              final IntentCommonService intentCommonService) {
        super();
        this.dataBroker = dataBroker;
        this.intentCommonService = intentCommonService;
    }

    @Override
    public void start() {
        LOG.info("\nIntentVlanListener initiated with success.");
        final DataTreeIdentifier identifier = new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, InstanceIdentifierUtils.INTENT_VLANS_IDENTIFIER);
        registration = dataBroker.registerDataTreeChangeListener(identifier, this);
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<IntentVlans>> collection) {
        super.handleIntentTreeEvent(collection);
    }

    @Override
    void handleIntentCreated(IntentVlans intents) {
        intents.getIntentVlan().forEach(intent -> intentCommonService.resolveAndApply(intent));
    }

    @Override
    void handleIntentUpdated(IntentVlans intent) {
        //TODO: Implement update
    }

    @Override
    void handleIntentRemoved(IntentVlans intent) {
        //TODO: Implement delete method
    }

    @Override
    public void stop() {
        registration.close();
    }

}
