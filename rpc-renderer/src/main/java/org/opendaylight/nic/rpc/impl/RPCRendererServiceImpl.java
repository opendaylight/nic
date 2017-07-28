/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.rpc.api.RPCRendererService;
import org.opendaylight.nic.rpc.rest.JuniperRestService;
import org.opendaylight.nic.rpc.utils.InstanceIdentifierUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.EvpnDataflows;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created by yrineu on 14/07/17.
 */
public class RPCRendererServiceImpl implements RPCRendererService {
    private static final Logger LOG = LoggerFactory.getLogger(RPCRendererServiceImpl.class);

    private DataBroker dataBroker;
    private JuniperRestService juniperRestService;

    private ListenerRegistration registration;

    public RPCRendererServiceImpl(final DataBroker dataBroker,
                                  final JuniperRestService juniperRestService) {
        this.dataBroker = dataBroker;
        this.juniperRestService = juniperRestService;
    }

    public void start() {
        final DataTreeIdentifier dataTreeIdentifier = new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, InstanceIdentifierUtils.EVPN_DATAFLOW_IDENTIFIER);
        registration = dataBroker.registerDataTreeChangeListener(dataTreeIdentifier, this);
        LOG.info("\nRPC Renderer Service session initialized.");
    }

    public void stop() {
        registration.close();
    }


    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<EvpnDataflows>> collection) {
        collection.iterator().forEachRemaining(evpnDataflowTree -> {
            final EvpnDataflows evpnDataflows = evpnDataflowTree.getRootNode().getDataAfter();

            switch (evpnDataflowTree.getRootNode().getModificationType()) {
                case WRITE:
                    LOG.info("\n### Sending configuration for a WRITE on EvpnTree.");
                    juniperRestService.sendConfiguration(evpnDataflows.getEvpnDataflow());
                    break;
                case SUBTREE_MODIFIED:
                    LOG.info("\n### Sending configuration for a SUBTREE_MODIFIED on EvpnTree.");
                    juniperRestService.sendConfiguration(evpnDataflows.getEvpnDataflow());
                    break;
            }
        });
    }
}
