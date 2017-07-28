/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.protocol.evpn;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.nic.rpc.model.juniper.configuration.vlan.EvpnEncapsulationType;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Set;

/**
 * Created by yrineu on 20/07/17.
 */
@PrepareForTest(Evpn.class)
@RunWith(PowerMockRunner.class)
public class EvpnTest {

    @Test
    public void testRpcStructure() {
        final StringBuffer result = new StringBuffer();
        final Set<Integer> vlans = Sets.newHashSet();
//        for (int i = 100; i <= 200; i++) {
//            vlans.add(i);
//        }
        vlans.add(100);
        final Evpn evpn = Evpn.create(vlans, EvpnEncapsulationType.vxlan, result);
        evpn.generateRPCConfigs();
        System.out.println(result);
        Assert.assertEquals(getStructure(), result.toString());
    }

    private String getStructure() {
        return "<evpn><vni-options><vni><name>100</name><vrf-target><community>target:10000:100</community>" +
                "</vrf-target></vni></vni-options><encapsulation>vxlan</encapsulation>" +
                "<extended-vni-list>100</extended-vni-list><multicast-mode>ingress-replication</multicast-mode></evpn>";
    }
}
