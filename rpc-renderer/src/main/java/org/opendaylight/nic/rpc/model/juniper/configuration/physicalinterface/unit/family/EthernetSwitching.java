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

    private EthernetSwitching(final Set<String> vlanMembers,
                              final StringBuffer result) {
        this.vlanMembers = vlanMembers;
        this.result = result;
    }

    public static EthernetSwitching create(final Set<String> vlanNames,
                                           final StringBuffer buffer) {
        return new EthernetSwitching(vlanNames, buffer);
    }

    protected String generateRPCStructure() {
        result.append("<ethernet-switching>");
        if (vlanMembers.size() > 1) {
            result.append("<interface-mode>");
            result.append("trunk");
            result.append("</interface-mode>");
        }
        result.append("<vlan>");
        vlanMembers.forEach(vlanName -> result.append("<members>" + vlanName.replace(" ", "_") + "</members>"));
        result.append("</vlan>");
        result.append("</ethernet-switching>");
        return result.toString();
    }
}
