/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.impl;

import com.google.common.collect.Lists;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.rpc.api.RPCRendererService;
import org.opendaylight.nic.rpc.rest.JuniperRestService;
import org.opendaylight.nic.rpc.utils.InstanceIdentifierUtils;
import org.opendaylight.nic.rpc.utils.RPCRendererUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.queue.rev170807.EvpnDataflowQueues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.queue.rev170807.evpn.dataflow.queues.EvpnDataflowQueue;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * Created by yrineu on 14/07/17.
 */
public class EvpnRendererService implements RPCRendererService<EvpnDataflowQueues> {
    private static final Logger LOG = LoggerFactory.getLogger(EvpnRendererService.class);

    private DataBroker dataBroker;
    private JuniperRestService juniperRestService;

    private ListenerRegistration registration;
    private final RPCRendererUtils rendererUtils;

    public EvpnRendererService(final DataBroker dataBroker,
                               final JuniperRestService juniperRestService) {
        this.dataBroker = dataBroker;
        this.juniperRestService = juniperRestService;
        rendererUtils = new RPCRendererUtils(dataBroker);
    }

    @Override
    public void start() {
        final DataTreeIdentifier dataTreeIdentifier = new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, InstanceIdentifierUtils.EVPN_DATA_FLOW_QUEUES);
        registration = dataBroker.registerDataTreeChangeListener(dataTreeIdentifier, this);
        LOG.info("\nRPC Renderer Service session initialized.");
    }

    @Override
    public void stop() {
        registration.close();
    }


    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<EvpnDataflowQueues>> collection) {
        collection.iterator().forEachRemaining(evpnDataflowTree -> {
            final DataObjectModification<EvpnDataflowQueues> modification = evpnDataflowTree.getRootNode();
            switch (modification.getModificationType()) {
                case WRITE:
                    juniperRestService.sendConfiguration(modification.getDataAfter().getEvpnDataflowQueue(), false);
                    break;
                case SUBTREE_MODIFIED:
                    final List<EvpnDataflowQueue> before = Lists.newArrayList();
                    if (modification.getDataBefore() != null) {
                        before.addAll(modification.getDataBefore().getEvpnDataflowQueue());
                    }
                    final List<EvpnDataflowQueue> after = modification.getDataAfter().getEvpnDataflowQueue();
                    if (isElementAddedToSubTree(before, after)) {
                        after.removeAll(before);
                        juniperRestService.sendConfiguration(after, false);
                    } else if (isElementRemovedFromSubTree(before, after)) {
                        before.removeAll(after);
                        juniperRestService.sendConfiguration(before, true);
                    } else {
                        //TODO: Implement rollback and make it thread safe
                        juniperRestService.sendConfiguration(before, true);
                        juniperRestService.sendConfiguration(after, false);
                    }
                    break;
                case DELETE:
                    final EvpnDataflowQueues removedQueue = evpnDataflowTree.getRootNode().getDataBefore();
                    juniperRestService.sendConfiguration(removedQueue.getEvpnDataflowQueue(), true);
                    break;
            }
        });
    }

    private synchronized boolean isElementAddedToSubTree(final List<EvpnDataflowQueue> before,
                                                         final List<EvpnDataflowQueue> after) {
        return (before.size() < after.size());
    }

    private synchronized boolean isElementRemovedFromSubTree(final List<EvpnDataflowQueue> before,
                                                             final List<EvpnDataflowQueue> after) {
        return (before.size() > after.size());
    }
}
