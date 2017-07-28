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
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.EvpnDataflows;
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
    public static final InstanceIdentifier<EvpnDataflows> EVPN_DATAFLOWS_IDENTIFIER =
            InstanceIdentifier.builder(EvpnDataflows.class).build();
    public static final InstanceIdentifier<HostInfos> HOST_INFOS_IDENTIFIER =
            InstanceIdentifier.builder(HostInfos.class).build();
}
