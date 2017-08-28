/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.vlan;

import com.google.common.collect.Sets;
import org.opendaylight.nic.rpc.model.juniper.configuration.ConfigurationInterface;

import java.util.Map;
import java.util.Set;

/**
 * Created by yrineu on 21/07/17.
 */
public class VlanConfig implements ConfigurationInterface{

    private StringBuffer result;
    private Map<String, Integer> vlanNameById;
    private EvpnEncapsulationType vlanType;
    private Boolean isADeleteSchema = false;

    private VlanConfig(final Map<String, Integer> vlanNameById,
                       final EvpnEncapsulationType vlanType,
                       final StringBuffer result) {
        this.vlanNameById = vlanNameById;
        this.vlanType = vlanType;
        this.result = result;
    }

    public static VlanConfig create(final Map<String, Integer> vlanNameById,
                                    final String vlanType,
                                    final StringBuffer buffer) {
        return new VlanConfig(vlanNameById, EvpnEncapsulationType.valueOf(vlanType), buffer);
    }

    @Override
    public void generateRPCStructure() {
        result.append("<vlans>");
        vlanNameById.entrySet().forEach(entry -> {
            if (!this.isADeleteSchema) {
                result.append("<vlan>");
            } else {
                result.append("<vlan delete=\"delete\">");
            }
            result.append("<name>" + entry.getKey().replace(" ", "_") + "</name>");
            result.append("<vlan-id>" + entry.getValue() + "</vlan-id>");
            switch (vlanType) {
                case vxlan:
                    result.append("<" + vlanType.name() + ">");
                    result.append("<vni>" + entry.getValue() + "</vni>");
                    result.append("<ingress-node-replication/>");
                    result.append("</" + vlanType.name() + ">");
            }
            result.append("</vlan>");
        });
        result.append("</vlans>");
    }

    @Override
    public void generateDeleteRPCStructure() {
        this.isADeleteSchema = true;
        generateRPCStructure();
    }

    public Set<String> getVlanNames() {
        return vlanNameById.keySet();
    }

    public Map<String, Integer> getVlanNameById() {
        return vlanNameById;
    }

    public Set<Integer> getVlanIds() {
        return Sets.newHashSet(vlanNameById.values());
    }
}
