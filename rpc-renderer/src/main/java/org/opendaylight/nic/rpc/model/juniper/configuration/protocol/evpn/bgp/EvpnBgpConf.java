/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.protocol.evpn.bgp;

import org.opendaylight.nic.rpc.model.juniper.configuration.protocol.ProtocolConfInterface;

import java.util.Set;

/**
 * Created by yrineu on 26/07/17.
 */
public class EvpnBgpConf implements ProtocolConfInterface {

    private StringBuffer result;
    private String name;
    private String groupType;
    private String localAddress;
    private Set<String> neighbors;
    private Boolean isADeleteSchema;

    private EvpnBgpConf(final String name,
                        final String groupType,
                        final String localAddress,
                        final Set<String> neighbors,
                        final StringBuffer result) {
        this.name = name;
        this.groupType = groupType;
        this.localAddress = localAddress;
        this.neighbors = neighbors;
        this.result = result;
    }

    public static EvpnBgpConf create(final String name,
                                     final String groupType,
                                     final String localAddress,
                                     final Set<String> neighbors,
                                     final StringBuffer buffer) {
        return new EvpnBgpConf(name, groupType, localAddress, neighbors, buffer);
    }

    @Override
    public void generateRPCStructure() {
        result.append("<bgp>");
        if (!isADeleteSchema) {
            result.append("<group>");
        } else {
            result.append("<group delete=\"delete\">");
        }
        result.append("<name>");
        result.append(name.replace(" ", "_"));
        result.append("</name>");
        result.append("<local-address>");
        result.append(localAddress);
        result.append("</local-address>");
        result.append("<family>");
        result.append("<evpn>");
        result.append("<signaling>");
        result.append("</signaling>");
        result.append("</evpn>");
        result.append("</family>");
        generateNeighborsStructure();
        result.append("</group>");
        result.append("</bgp>");
    }

    private void generateNeighborsStructure() {
        neighbors.forEach(neighbor -> {
            result.append("<neighbor>");
            result.append("<name>");
            result.append(neighbor);
            result.append("</name>");
            result.append("</neighbor>");
        });
    }

    @Override
    public void generateDeleteRPCStructure() {
        this.isADeleteSchema = true;
        generateRPCStructure();
    }
}
