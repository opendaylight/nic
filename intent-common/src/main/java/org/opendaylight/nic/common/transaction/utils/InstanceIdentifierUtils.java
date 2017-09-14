/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.utils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.evpn.rev170724.IntentEvpns;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.isp.prefix.rev170615.IntentIspPrefixes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.IntentsLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711.SwitchInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host.info.rev170724.HostInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.pod.info.rev700101.PodInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.service.mapping.rev170801.ServiceMappings;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811.SwitchInterfacesStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.queue.rev170807.EvpnDataflowQueues;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by yrineu on 28/06/17.
 */
public class InstanceIdentifierUtils {

    public static final InstanceIdentifier<IntentsLimiter> INTENTS_LIMITER_IDENTIFIER =
            InstanceIdentifier.builder(IntentsLimiter.class).build();
    public static final InstanceIdentifier<IntentIspPrefixes> INTENT_ISP_PREFIXES_IDENTIFIER =
            InstanceIdentifier.builder(IntentIspPrefixes.class).build();
    public static final InstanceIdentifier<Intents> INTENTS_FIREWALL_IDENTIFIER =
            InstanceIdentifier.builder(Intents.class).build();
    public static final InstanceIdentifier<IntentEvpns> INTENT_EVPN_IDENTIFIER =
            InstanceIdentifier.builder(IntentEvpns.class).build();
    public static final InstanceIdentifier<SwitchInfos> SWITCH_INFOS_IDENTIFIER =
            InstanceIdentifier.create(SwitchInfos.class);
    public static final InstanceIdentifier<EvpnDataflowQueues> EVPN_DATAFLOW_QUEUES_IDENTIFIER =
            InstanceIdentifier.create(EvpnDataflowQueues.class);
    public static final InstanceIdentifier<HostInfos> HOST_INFOS_IDENTIFIER =
            InstanceIdentifier.builder(HostInfos.class).build();
    public static final InstanceIdentifier<ServiceMappings> SERVICE_MAPPINGS_IDENTIFIER =
            InstanceIdentifier.builder(ServiceMappings.class).build();
    public static final InstanceIdentifier<SwitchInterfacesStatus> SWITCH_INTERFACES_STATUS_IDENTIFIER =
            InstanceIdentifier.create(SwitchInterfacesStatus.class);
    public static final InstanceIdentifier<PodInfos> POD_INFOS_IDENTIFIER =
            InstanceIdentifier.create(PodInfos.class);
}
