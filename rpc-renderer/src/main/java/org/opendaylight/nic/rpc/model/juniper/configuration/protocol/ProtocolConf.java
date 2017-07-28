/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.protocol;

import org.opendaylight.nic.rpc.model.juniper.configuration.protocol.evpn.Evpn;
import org.opendaylight.nic.rpc.model.juniper.configuration.protocol.evpn.bgp.EvpnBgpConf;
import org.opendaylight.nic.rpc.model.juniper.configuration.protocol.ospf.OSPFConf;
import org.opendaylight.nic.rpc.model.juniper.configuration.vlan.EvpnEncapsulationType;

import java.util.Map;
import java.util.Set;

/**
 * Created by yrineu on 24/07/17.
 */
public class ProtocolConf {

    private StringBuffer result;
    private Evpn evpn;
    private OSPFConf ospfConf;
    private EvpnBgpConf evpnBgpConf;

    public ProtocolConf(final StringBuffer result) {
        this.result = result;
    }


    public void createEvpnConfigs(final Set<Integer> vlans) {
        this.evpn = Evpn.create(vlans, EvpnEncapsulationType.vxlan, result);
    }

    public void createOSPFConfigs(final String ospfName,
                                  final Map<String, String> interfaceNameByType) {
        this.ospfConf = OSPFConf.create(ospfName, interfaceNameByType, result);
    }

    public void createEvpnBgpConfigs(final String name,
                                     final String groupType,
                                     final String localAddress,
                                     final Set<String> neighbors,
                                     final StringBuffer buffer) {
        this.evpnBgpConf = EvpnBgpConf.create(name, groupType, localAddress, neighbors, buffer);
    }

    public void generateRPCStructure() {
        result.append("<protocols>");
        if (evpn != null) {
            evpn.generateRPCConfigs();
        }
        if (ospfConf != null) {
//            result.append(ospfConf.generateRPCStructure());
        }

        if (evpnBgpConf != null) {
//            result.append(evpnBgpConf.generateRPCStructure());
        }
        result.append("</protocols>");
    }
}
