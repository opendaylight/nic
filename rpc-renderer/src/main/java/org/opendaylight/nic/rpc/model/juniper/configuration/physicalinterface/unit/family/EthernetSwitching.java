/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.physicalinterface.unit.family;

import java.util.Set;

/**
 * Created by yrineu on 21/07/17.
 */
public class EthernetSwitching {

    private StringBuffer result;
    private Set<String> vlanMembers;
    private Boolean isADeleteSchema = false;

    private EthernetSwitching(final Set<String> vlanMembers,
                              final StringBuffer result) {
        this.vlanMembers = vlanMembers;
        this.result = result;
    }

    public static EthernetSwitching create(final Set<String> vlanNames,
                                           final StringBuffer buffer) {
        return new EthernetSwitching(vlanNames, buffer);
    }

    protected void generateRPCStructure() {
        result.append("<ethernet-switching>");
        generateInterfaceModeOptionIfNeed();
        if (!this.isADeleteSchema) {
            result.append("<vlan>");
        } else {
            result.append("<vlan delete=\"delete\">");
        }
        vlanMembers.forEach(vlanName -> generateVlanMembers(vlanName));
        result.append("</vlan>");
        result.append("</ethernet-switching>");
    }

    private void generateInterfaceModeOptionIfNeed() {
        final String TRUNK = "trunk";
        if (vlanMembers.size() > 1) {
            if (!isADeleteSchema) {
                result.append("<interface-mode>");
            } else {
                result.append("<interface-mode delete=\"delete\">");
            }
            result.append(TRUNK);
            result.append("</interface-mode>");
        }
    }

    private void generateVlanMembers(final String vlanName) {
        if (!isADeleteSchema) {
            result.append("<members>" + vlanName.replace(" ", "_") + "</members>");
        } else {
            result.append("<members delete=\"delete\">" + vlanName.replace(" ", "_") + "</members>");
        }
    }

    protected void generateDeleteRPCStructure() {
        this.isADeleteSchema = true;
        generateRPCStructure();
    }

    protected Set<String> getAggregatedVlans() {
        return vlanMembers;
    }
}
