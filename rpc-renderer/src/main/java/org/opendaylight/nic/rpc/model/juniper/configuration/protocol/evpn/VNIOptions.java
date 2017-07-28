/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.protocol.evpn;

import org.opendaylight.nic.rpc.model.juniper.configuration.ConfigurationUtils;

import java.util.Set;

/**
 * Created by yrineu on 20/07/17.
 */
public class VNIOptions {

    private StringBuffer result;
    private Set<Integer> vlans;

    private VNIOptions(final Set<Integer> vlans,
                       final StringBuffer result) {
        this.vlans = vlans;
        this.result = result;
    }

    protected static VNIOptions create(final Set<Integer> vlans,
                                       final StringBuffer buffer) {
        return new VNIOptions(vlans, buffer);
    }

    protected void generateVNIOptionsRequest() {
        result.append("<vni-options>");
        vlans.forEach(vlan -> generateVNI(vlan));
        result.append("</vni-options>");
    }

    private void generateVNI(final Integer vlan) {
        result.append("<vni>");
        result.append("<name>");
        result.append(vlan);
        result.append("</name>");
        result.append("<vrf-target>");
        result.append("<community>");
        result.append("target:" + ConfigurationUtils.VRF_TARGET + ":" + vlan);
        result.append("</community>");
        result.append("</vrf-target>");
        result.append("</vni>");
    }

    protected void generateExtendedVniList() {
        vlans.forEach(vlan -> result.append("<extended-vni-list>" + vlan + "</extended-vni-list>"));
    }
}
