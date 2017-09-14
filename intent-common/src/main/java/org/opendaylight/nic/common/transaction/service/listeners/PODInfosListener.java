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
import org.opendaylight.nic.common.transaction.api.NetworkMappingService;
import org.opendaylight.nic.common.transaction.utils.InstanceIdentifierUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.pod.info.rev700101.PodInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.pod.info.rev700101.pod.infos.PodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Created by yrineu on 14/09/17.
 */
public class PODInfosListener extends AbstractListener<PodInfos, PodInfo>{
    private static final Logger LOG = LoggerFactory.getLogger(PODInfosListener.class);

    private final NetworkMappingService networkMappingService;

    public PODInfosListener(final DataBroker dataBroker,
                            final NetworkMappingService networkMappingService) {
        super(dataBroker);
        this.networkMappingService = networkMappingService;
    }

    @Override
    public void start() {
        final DataTreeIdentifier<PodInfos> dataTreeIdentifier = new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, InstanceIdentifierUtils.POD_INFOS_IDENTIFIER);
        super.registerForDataTreeChanges(dataTreeIdentifier);
        LOG.info("\n### POD Info Listener started.");
    }

    @Override
    void handleTreeCreated(PodInfos data) {
        LOG.info("\n### POD Info received.");
        data.getPodInfo().forEach(podInfo -> networkMappingService.processNetworkMappingChange(podInfo));
    }

    @Override
    void handleSubTreeChange(PodInfos before, PodInfos after) {
        after.getPodInfo().forEach(podInfo -> networkMappingService.processNetworkMappingChange(podInfo));
        LOG.info("\n### POD Info Subtree changed.");
    }

    @Override
    void handleTreeRemoved(PodInfos intent) {

    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<PodInfos>> collection) {
        super.handleTreeEvent(collection);
    }

    @Override
    public void stop() {

    }
}
