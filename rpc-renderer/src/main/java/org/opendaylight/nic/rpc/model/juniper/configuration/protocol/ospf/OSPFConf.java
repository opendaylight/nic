/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.protocol.ospf;

import java.util.Map;

/**
 * Created by yrineu on 25/07/17.
 */
public class OSPFConf {

    private StringBuffer result;
    private String name;
    private Map<String, String> interfaceNameByType;

    private OSPFConf() {
    }

    private OSPFConf(final String name,
                     final Map<String, String> interfaceNameByType,
                     final StringBuffer result) {
        this.name = name;
        this.interfaceNameByType = interfaceNameByType;
        this.result = result;
    }

    public static OSPFConf create(final String name,
                                  final Map<String, String> interfaceNameByType,
                                  final StringBuffer buffer) {
        return new OSPFConf(name, interfaceNameByType, buffer);
    }

    public String generateRPCStructure() {
        result.append("<ospf>");
        result.append("<area>");
        result.append("<name>");
        result.append(name);
        result.append("</name>");
        result.append(generateInterfacesStructure());
        result.append("</area>");
        result.append("</ospf>");
        return result.toString();
    }

    private String generateInterfacesStructure() {
        interfaceNameByType.entrySet().forEach(entry -> {
            result.append("<interface>");
            result.append("<name>");
            result.append(entry.getKey());
            result.append("</name>");
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                result.append("<interface-type>");
                result.append(entry.getValue().toLowerCase());
                result.append("</interface-type>");
            }
            result.append("</interface>");
        });
        return result.toString();
    }
}
