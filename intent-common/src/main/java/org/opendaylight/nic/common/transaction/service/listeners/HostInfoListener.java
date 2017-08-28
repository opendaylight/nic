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
import org.opendaylight.nic.common.transaction.service.renderer.RPCRendererUtils;
import org.opendaylight.nic.common.transaction.utils.CommonUtils;
import org.opendaylight.nic.common.transaction.utils.InstanceIdentifierUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host.info.rev170724.HostInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host.info.rev170724.host.infos.HostInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * Created by yrineu on 22/08/17.
 */
public class HostInfoListener extends AbstractListener<HostInfos, HostInfo> {

    private static final Logger LOG = LoggerFactory.getLogger(HostInfoListener.class);
    private CommonUtils commonUtils;
    private RPCRendererUtils rpcRendererUtils;

    public HostInfoListener(final DataBroker dataBroker) {
        super(dataBroker);
        this.commonUtils = new CommonUtils(dataBroker);
        this.rpcRendererUtils = new RPCRendererUtils(commonUtils);
    }

    @Override
    public void start() {
        final DataTreeIdentifier identifier = new DataTreeIdentifier<>(
                LogicalDatastoreType.CONFIGURATION, InstanceIdentifierUtils.HOST_INFOS_IDENTIFIER);
        super.registerForDataTreeChanges(identifier);
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<HostInfos>> collection) {
        super.handleTreeEvent(collection);
    }

    @Override
    void handleTreeCreated(HostInfos hostInfos) {
        rpcRendererUtils.handleHostInfoChanges(hostInfos.getHostInfo());
    }

    @Override
    void handleSubTreeChange(HostInfos hostInfosBefore, HostInfos hostInfosAfter) {
        final List<HostInfo> before = hostInfosBefore.getHostInfo();
        final List<HostInfo> after = hostInfosAfter.getHostInfo();

        final Boolean isElementAdded = super.isSubTreeElementAdded(before, after);
        if (isElementAdded) {
            after.removeAll(before);
            before.addAll(after);
            rpcRendererUtils.handleHostInfoChanges(before);
        } else if (super.isSubTreeElementRemoved(before, after)) {
            before.removeAll(after);
            rpcRendererUtils.handleHostInfoChanges(before);
        }
    }

    @Override
    void handleTreeRemoved(HostInfos hostInfos) {
        LOG.info("\n#### HostInfos removed.");
    }

    @Override
    public void stop() {
        super.closeDataTreeRegistration();
    }
}
