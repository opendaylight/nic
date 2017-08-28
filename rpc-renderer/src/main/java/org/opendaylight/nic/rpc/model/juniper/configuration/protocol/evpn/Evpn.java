/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.protocol.evpn;

import org.opendaylight.nic.rpc.model.juniper.configuration.protocol.ProtocolConfInterface;
import org.opendaylight.nic.rpc.model.juniper.configuration.vlan.EvpnEncapsulationType;

import java.util.Set;

/**
 * Created by yrineu on 20/07/17.
 */
public class Evpn implements ProtocolConfInterface {

    private StringBuffer result;
    private Set<Integer> vlans;
    private EvpnEncapsulationType encapsulation;
    private Boolean isADeleteSchema = false;

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

    @Override
    public void generateRPCStructure() {
        this.vniOptions = VNIOptions.create(vlans, result);
        result.append("<evpn>");
        if (!isADeleteSchema) {
            vniOptions.generateRPCStructure();
        } else {
            vniOptions.generateDeleteRPCStructure();
        }
        result.append("<encapsulation>" + encapsulation.name() + "</encapsulation>");
        if (!isADeleteSchema) {
            vniOptions.generateExtendedVniList();
        } else {
            vniOptions.generateDeleteRPCStructure();
        }
        result.append("<multicast-mode>" + MulticastMode.INGRESS_REPLICATION.getValue() + "</multicast-mode>");
        result.append("</evpn>");
    }

    @Override
    public void generateDeleteRPCStructure() {
        this.isADeleteSchema = true;
        generateRPCStructure();
    }
}
