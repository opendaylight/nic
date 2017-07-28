/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.protocol.evpn;

import org.opendaylight.nic.rpc.model.juniper.configuration.vlan.EvpnEncapsulationType;

import java.util.Set;

/**
 * Created by yrineu on 20/07/17.
 */
public class Evpn {

    private StringBuffer result;
    private Set<Integer> vlans;
    private EvpnEncapsulationType encapsulation;

    private VNIOptions vniOptions;

    private Evpn(final Set<Integer> vlans,
                 final EvpnEncapsulationType encapsulation,
                 final StringBuffer result) {
        this.vlans = vlans;
        this.encapsulation = encapsulation;
        this.result = result;
    }

    public static Evpn create(final Set<Integer> vlans,
                              final EvpnEncapsulationType encapsulation,
                              final StringBuffer buffer) {
        return new Evpn(vlans, encapsulation, buffer);
    }

    public void generateRPCConfigs() {
        this.vniOptions = VNIOptions.create(vlans, result);
        result.append("<evpn>");
        vniOptions.generateVNIOptionsRequest();
        result.append("<encapsulation>" + encapsulation.name() + "</encapsulation>");
        vniOptions.generateExtendedVniList();
        result.append("<multicast-mode>" + MulticastMode.INGRESS_REPLICATION.getValue() + "</multicast-mode>");
        result.append("</evpn>");
    }
}
