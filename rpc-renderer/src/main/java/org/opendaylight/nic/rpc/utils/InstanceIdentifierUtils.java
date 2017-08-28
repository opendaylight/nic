/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.utils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711.SwitchInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811.SwitchInterfacesStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.queue.rev170807.EvpnDataflowQueues;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by yrineu on 25/07/17.
 */
public class InstanceIdentifierUtils {

    public static final InstanceIdentifier<EvpnDataflowQueues> EVPN_DATA_FLOW_QUEUES = InstanceIdentifier
            .builder(EvpnDataflowQueues.class).build();
    public static final InstanceIdentifier<SwitchInterfacesStatus> SWITCH_INTERFACES_STATUS_IDENTIFIER =
            InstanceIdentifier.create(SwitchInterfacesStatus.class);
    public static final InstanceIdentifier<SwitchInfos> SWITCH_INFOS_IDENTIFIER =
            InstanceIdentifier.create(SwitchInfos.class);
}
