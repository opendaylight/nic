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
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.IntentsLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created by yrineu on 28/06/17.
 */
public class IntentLimiterListener implements IntentTreeChangesListener<IntentsLimiter> {
    private static final Logger LOG = LoggerFactory.getLogger(IntentLimiterListener.class);
    private final DataBroker dataBroker;
    private final IntentCommonService intentCommonService;

    public IntentLimiterListener(final DataBroker dataBroker,
                                 final IntentCommonService intentCommonService) {
        this.dataBroker = dataBroker;
        this.intentCommonService = intentCommonService;
    }

    @Override
    public void start() {
        LOG.info("\nIntentLimiterListener started with success!!!");
        final DataTreeIdentifier<IntentsLimiter> dataTreeIdentifier = new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, InstanceIdentifierUtils.INTENTS_LIMITER_IDENTIFIER);
        dataBroker.registerDataTreeChangeListener(dataTreeIdentifier, this);
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<IntentsLimiter>> collection) {
        LOG.info("\nEvent received for an IntentLimiter!! {}", collection.toString());
        collection.iterator().forEachRemaining(intentTree -> {
            final IntentsLimiter intents = intentTree.getRootNode().getDataAfter();
            intents.getIntentLimiter().forEach(intentLimiter -> intentCommonService.resolveAndApply(intentLimiter));
        });
    }

    @Override
    public void stop() {
        //TODO: Implement a cleanup
    }
}
