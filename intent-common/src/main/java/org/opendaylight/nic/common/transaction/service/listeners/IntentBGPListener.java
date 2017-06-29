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
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.isp.prefix.rev170615.intent.isp.prefixes.IntentIspPrefix;
import org.opendaylight.yangtools.concepts.ListenerRegistration;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created by yrineu on 28/06/17.
 */
public class IntentBGPListener implements IntentTreeChangesListener<IntentIspPrefix> {

    private final DataBroker dataBroker;
    private final IntentCommonService intentCommonService;
    private ListenerRegistration listenerRegistration;

    public IntentBGPListener(final DataBroker dataBroker,
                             final IntentCommonService intentCommonService) {
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
    public void stop() {
        listenerRegistration.close();
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<IntentIspPrefix>> collection) {
        collection.iterator().forEachRemaining(intentTree -> {
            final IntentIspPrefix ispPrefix = intentTree.getRootNode().getDataAfter();
            intentCommonService.resolveAndApply(ispPrefix);
        });
    }
}
