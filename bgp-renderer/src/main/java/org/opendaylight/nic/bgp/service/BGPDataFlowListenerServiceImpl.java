/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.bgp.service;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.bgp.api.BGPRendererService;
import org.opendaylight.nic.bgp.utils.Utils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.BgpDataflows;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yrineu on 20/06/17.
 */
public class BGPDataFlowListenerServiceImpl implements BGPDataFlowListenerService {
    private static final Logger LOG = LoggerFactory.getLogger(BGPDataFlowListenerServiceImpl.class);

    private final DataBroker dataBroker;
    private final BGPRendererService bgpRendererService;
    private ListenerRegistration<DataTreeChangeListener> dataflowListenerRegistration;

    public BGPDataFlowListenerServiceImpl(final DataBroker dataBroker, final BGPRendererService bgpRendererService) {
        this.dataBroker = dataBroker;
        this.bgpRendererService = bgpRendererService;
    }

    @Override
    public void start() {
        LOG.info("\nBGP Session Initiated");
        final DataTreeIdentifier dataTreeIdentifier = new DataTreeIdentifier(LogicalDatastoreType.CONFIGURATION,
                Utils.BGP_DATAFLOW_IDENTIFIER);
        this.dataflowListenerRegistration = dataBroker.registerDataTreeChangeListener(dataTreeIdentifier, this);
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<BgpDataflows>> collection) {
        LOG.info("\nBGP Dataflow received.");
        collection.iterator().forEachRemaining(this::handleTreeChangeEvent);
    }

    //TODO: Controll those calls using a threadpool.
    private void handleTreeChangeEvent(final DataTreeModification<BgpDataflows> bgpDataFlows) {
        final DataObjectModification<BgpDataflows> modification = bgpDataFlows.getRootNode();
        BgpDataflows removedDataflows = modification.getDataBefore();
        BgpDataflows addedDataflows = modification.getDataAfter();
        switch (modification.getModificationType()) {
            case WRITE:
                addedDataflows.getBgpDataflow().forEach(bgpRendererService::advertiseRoute);
                break;
            case SUBTREE_MODIFIED:
                removedDataflows.getBgpDataflow().forEach(bgpRendererService::remoteRoute);
                addedDataflows.getBgpDataflow().forEach(bgpRendererService::advertiseRoute);
                break;
            case DELETE:
                removedDataflows.getBgpDataflow().forEach(bgpRendererService::remoteRoute);
                break;
            default:
                LOG.warn("\nNo actions for this BGP Dataflow change: {}", bgpDataFlows.getRootPath().toString());
        }
    }

    @Override
    public void stop() {
        dataflowListenerRegistration.close();
        bgpRendererService.stop();
    }
}
