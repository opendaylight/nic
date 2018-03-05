/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.utils;

import com.google.common.base.Optional;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711.SwitchInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711.SwitchName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711._switch.infos.SwitchInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711._switch.infos.SwitchInfoKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811.SwitchInterfacesStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811._switch.interfaces.status.SwitchInterfaceStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811._switch.interfaces.status.SwitchInterfaceStatusKey;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yrineu on 14/08/17.
 */
public class RPCRendererUtils {

    private interface VLANCountAction {
        Integer doExecute(final AtomicInteger count);
    }

    private static final Logger LOG = LoggerFactory.getLogger(RPCRendererUtils.class);
    private final DataBroker dataBroker;

    final InstanceIdentifier<SwitchInterfacesStatus> switchInterfacesStatusIdentifier = InstanceIdentifier.builder(SwitchInterfacesStatus.class).build();
    final InstanceIdentifier<SwitchInfos> switchInfoIdentifier = InstanceIdentifier.builder(SwitchInfos.class).build();

    public RPCRendererUtils(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public SwitchInterfaceStatus retrieveSwitchInterfaceStatus(final String switchId) {
        SwitchInterfaceStatus result = null;
        final InstanceIdentifier<SwitchInterfaceStatus> identifier = switchInterfacesStatusIdentifier
                .child(SwitchInterfaceStatus.class, new SwitchInterfaceStatusKey(switchId));
        try {
            result = retrieve(identifier);
        } catch (ReadFailedException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    public SwitchInfo retrieveSwitchInfo(final String id) {
        SwitchInfo result = null;
        final InstanceIdentifier<SwitchInfo> identifier = switchInfoIdentifier.child(
                SwitchInfo.class, new SwitchInfoKey(new SwitchName(id)));
        try {
            result = retrieve(identifier);
        } catch (ReadFailedException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    private <T extends DataObject> T retrieve(final InstanceIdentifier<T> identifier) throws ReadFailedException {
        T result = null;
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
        final Optional<T> optional = transaction.read(LogicalDatastoreType.CONFIGURATION, identifier).checkedGet();
        if (optional.isPresent()) {
            result = optional.get();
        }
        return result;
    }
}
