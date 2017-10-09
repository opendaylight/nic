/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.bgp.service;

import org.opendaylight.controller.md.sal.binding.api.*;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.bgp.api.BGPRendererService;
import org.opendaylight.nic.bgp.utils.Utils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.BgpDataflows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.bgp.dataflows.BgpDataflow;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created by yrineu on 20/06/17.
 */
public class BGPDataFlowListenerServiceImpl implements BGPDataFlowListenerService {
    private static final Logger LOG = LoggerFactory.getLogger(BGPDataFlowListenerServiceImpl.class);

    private final DataBroker dataBroker;
    private final BGPRendererService bgpRendererService;
    private ListenerRegistration<DataTreeChangeListener> dataflowListenerRegistration;

    public BGPDataFlowListenerServiceImpl(final DataBroker dataBroker,
                                          final BGPRendererService bgpRendererService) {
        this.dataBroker = dataBroker;
        this.bgpRendererService = bgpRendererService;
    }

    @Override
    public void start() {
        LOG.info("\nBGP Session Initiated");
        final DataTreeIdentifier dataTreeIdentifier = new DataTreeIdentifier(
                LogicalDatastoreType.CONFIGURATION,
                Utils.BGP_DATAFLOW_IDENTIFIER);
        this.dataflowListenerRegistration = dataBroker.registerDataTreeChangeListener(dataTreeIdentifier, this);
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<BgpDataflows>> collection) {
        LOG.info("\nBGP Dataflow received.");
        collection.iterator().forEachRemaining(consumer -> {
            final DataObjectModification<BgpDataflows> objectModification = consumer.getRootNode();
            final BgpDataflows bgpDataflowTree = objectModification.getDataAfter();
            bgpDataflowTree.getBgpDataflow().forEach(bgpDataflow -> bgpRendererService.advertiseRoute(bgpDataflow));
        });
    }

    @Override
    public void stop() {
        dataflowListenerRegistration.close();
        bgpRendererService.stop();
    }
}
