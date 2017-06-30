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
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.isp.prefix.rev170615.IntentIspPrefixes;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created by yrineu on 28/06/17.
 */
public class IntentBGPListener extends AbstractListener<IntentIspPrefixes>
        implements IntentTreeChangesListener<IntentIspPrefixes> {

    private final static Logger LOG = LoggerFactory.getLogger(IntentBGPListener.class);
    private final DataBroker dataBroker;
    private final IntentCommonService intentCommonService;
    private ListenerRegistration listenerRegistration;

    public IntentBGPListener(final DataBroker dataBroker,
                             final IntentCommonService intentCommonService) {
        super();
        this.dataBroker = dataBroker;
        this.intentCommonService = intentCommonService;
    }

    @Override
    public void start() {
        final DataTreeIdentifier treeIdentifier = new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, InstanceIdentifierUtils.INTENT_ISP_PREFIXES_IDENTIFIER);
        listenerRegistration = dataBroker.registerDataTreeChangeListener(treeIdentifier, this);
    }

    @Override
    public void handleIntentCreated(IntentIspPrefixes intents) {
        LOG.info("\n#### Creating IntentISPPrefix");
        intents.getIntentIspPrefix().forEach(intent -> intentCommonService.resolveAndApply(intent));
    }

    @Override
    public void handleIntentUpdated(IntentIspPrefixes intents) {
        //TODO: Implement update method
    }

    @Override
    public void handleIntentRemoved(IntentIspPrefixes intents) {
        //TODO: Implement update method
    }

    @Override
    public void stop() {
        listenerRegistration.close();
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<IntentIspPrefixes>> collection) {
       super.handleIntentTreeEvent(collection);
    }
}
