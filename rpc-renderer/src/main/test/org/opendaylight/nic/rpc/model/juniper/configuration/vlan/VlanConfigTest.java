/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.vlan;

import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;

/**
 * Created by yrineu on 28/07/17.
 */
@PrepareForTest(VlanConfig.class)
@RunWith(PowerMockRunner.class)
public class VlanConfigTest {

    @Test
    public void testValidateRPCStructure() {
        final StringBuffer result = new StringBuffer();
        final Map<String, Integer> vlanNameById = Maps.newHashMap();
        vlanNameById.put("Dev_Team", 100);
        VlanConfig.create(vlanNameById, EvpnEncapsulationType.vxlan.name(), result);

//        Assert.assertEquals(getDefaultStructure(), vlanConfig.generateRPCStructure());
    }

    private String getDefaultStructure() {
        return "<vlans><vlan><name>Dev_Team</name><vlan-id>100</vlan-id><vxlan>" +
                "<vni>100</vni><ingress-node-replication/></vxlan></vlan></vlans>";
    }
}
