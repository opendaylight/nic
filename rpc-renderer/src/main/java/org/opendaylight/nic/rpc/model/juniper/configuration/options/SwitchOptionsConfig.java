/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.options;

import org.opendaylight.nic.rpc.model.juniper.configuration.ConfigurationInterface;
import org.opendaylight.nic.rpc.model.juniper.configuration.ConfigurationUtils;

/**
 * Created by yrineu on 26/07/17.
 */
public class SwitchOptionsConfig implements ConfigurationInterface {

    private String loopbackIp;
    private StringBuffer result;
    private Boolean isADeleteSchema = false;

    public SwitchOptionsConfig(final StringBuffer result) {
        this.result = result;
    }

    public void create(final String loopbackIp) {
        this.loopbackIp = loopbackIp;
    }

    @Override
    public void generateRPCStructure() {
        if (!isADeleteSchema) {
            result.append("<switch-options>");
        } else {
            result.append("<switch-options delete=\"delete\">");
        }
        result.append("<route-distinguisher>");
        result.append("<rd-type>");
        result.append(loopbackIp + ":1");
        result.append("</rd-type>");
        result.append("</route-distinguisher>");
        result.append("<vrf-import>");
        result.append(ConfigurationUtils.EVPN_VRF_IMPORT);
        result.append("</vrf-import>");
        result.append("<vrf-target>");
        result.append("<community>");
        result.append(ConfigurationUtils.EVPN_VRF_TARGET_COMMUNITY);
        result.append("</community>");
        result.append("<auto>");
        result.append("</auto>");
        result.append("</vrf-target>");
        result.append("</switch-options>");
    }

    @Override
    public void generateDeleteRPCStructure() {
        this.isADeleteSchema = true;
        generateRPCStructure();
    }

    public String getLoopbackIp() {
        return loopbackIp;
    }
}
