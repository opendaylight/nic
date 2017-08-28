/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.policy;

import org.opendaylight.nic.rpc.model.juniper.configuration.ConfigurationInterface;
import org.opendaylight.nic.rpc.model.juniper.configuration.ConfigurationUtils;

/**
 * Created by yrineu on 21/07/17.
 */
public class Community implements ConfigurationInterface {

    private StringBuffer result;
    private Integer vni;
    private Boolean isADeleteSchema = false;

    private Community(final Integer vni,
                      final StringBuffer result) {
        this.vni = vni;
        this.result = result;
    }

    public static Community create(final Integer vni,
                                   final StringBuffer buffer) {
        return new Community(vni, buffer);
    }

    @Override
    public void generateRPCStructure() {
        if (!isADeleteSchema) {
            result.append("<community>");
        } else {
            result.append("<community delete=\"delete\">");
        }
        result.append("<name>vni"+vni+"</name>");
        result.append("<members>target:"+ ConfigurationUtils.VRF_TARGET+":"+vni+"</members>");
        result.append("</community>");
    }

    @Override
    public void generateDeleteRPCStructure() {
        this.isADeleteSchema = true;
        generateRPCStructure();
    }

    public static void generateEvpnStaticCommunity(final StringBuffer result) {
        result.append("<community>");
        result.append("<name>" + ConfigurationUtils.EVPN_POLICY_COMMUNITY + "</name>");
        result.append("<members>" + ConfigurationUtils.EVPN_VRF_TARGET_COMMUNITY + "</members>");
        result.append("</community>");
    }
}
