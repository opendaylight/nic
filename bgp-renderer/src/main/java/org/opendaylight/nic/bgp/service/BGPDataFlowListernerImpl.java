/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.bgp.service;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.bgp.api.BGPRendererService;
import org.opendaylight.nic.bgp.impl.BGPRouteServiceImpl;
import org.opendaylight.nic.bgp.utils.Utils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.bgp.dataflows.BgpDataflow;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created by yrineu on 20/06/17.
 */
public class BGPDataFlowListernerImpl implements BGPDataFlowListenerService {
    private static final Logger LOG = LoggerFactory.getLogger(BGPDataFlowListernerImpl.class);

    private final DataBroker dataBroker;
    private final BGPRendererService bgpRendererService;
    private ListenerRegistration<DataTreeChangeListener> dataflowListenerRegistration;

    public BGPDataFlowListernerImpl(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.bgpRendererService = new BGPRouteServiceImpl(dataBroker);
    }

    @Override
    public void start() {
        final DataTreeIdentifier dataTreeIdentifier = new DataTreeIdentifier(
                LogicalDatastoreType.CONFIGURATION,
                Utils.BGP_DATAFLOW_IDENTIFIER);
        this.dataflowListenerRegistration = dataBroker.registerDataTreeChangeListener(dataTreeIdentifier, this);
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<BgpDataflow>> collection) {
        LOG.info("\nBGPDataflow received.");
        collection.iterator().forEachRemaining(consumer -> {
            final BgpDataflow bgpDataflow = consumer.getRootNode().getDataAfter();
            bgpRendererService.advertiseRoute(bgpDataflow);
        });
    }

    @Override
    public void stop() {
        dataflowListenerRegistration.close();
        bgpRendererService.close();
    }
}
