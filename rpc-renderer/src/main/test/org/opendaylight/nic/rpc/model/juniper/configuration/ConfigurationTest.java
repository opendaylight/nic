/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.nic.rpc.model.juniper.configuration.Configuration;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;
import java.util.Set;

/**
 * Created by yrineu on 20/07/17.
 */
@PrepareForTest(Configuration.class)
@RunWith(PowerMockRunner.class)
public class ConfigurationTest {

    @Test
    public void testConfiguration() {
        final String intentName = "My_Intent";

        final String devTeamVlanName = "Dev_Team_vlan_100";
        final String adminTeamVlanName = "Admin_Team_vlan_200";
        final String financeTeamVlanName = "Finance_Team_vlan_300";

        final Integer vlanDev = 100;
        final Integer vlanAdmin = 200;
        final Integer vlanFinance = 300;

        final String interfaceName = "xe-0/0/0";
        final String loopbackIp = "10.200.19.63";

        final Map<String, Integer> vlanNameById = Maps.newHashMap();
        for (int i=100; i<=101; i++) {
        }
        vlanNameById.put(devTeamVlanName, 100);

        final Set<String> vlans = Sets.newHashSet();
        final Set<Integer> vlanIds = Sets.newHashSet();
        vlanNameById.entrySet().forEach(entry -> {
            vlans.add(entry.getKey());
            vlanIds.add(entry.getValue());
        });

        Configuration configuration = new Configuration();
        configuration.configureVxlans(vlanNameById);
        configuration.configurePolicyCommunityAccept(intentName, vlanIds);
        configuration.configureInterfaceVlans(interfaceName, vlans);
        configuration.configureEvpn(vlanIds);
        configuration.configureSwitchOptions(loopbackIp);

//        String configurationStructure = configuration.generateRPCStructure();

        System.out.println(configuration.generateRPCStructure());

//        Assert.assertEquals(getConfigurationRpcStructure(), configuration.generateRPCStructure());
    }

    private String getConfigurationRpcStructure() {
        return "<lock-configuration/><load-configuration><configuration><switch-options><route-distinguisher>" +
                "<rd-type>10.200.19.63:1</rd-type></route-distinguisher><vrf-import>switch_options_comm</vrf-import>" +
                "<vrf-target><community>target:65000:2</community><auto></auto></vrf-target></switch-options>" +
                "<interfaces><interface><name>xe-0/0/0</name><description>xe-0/0/0_</description><unit>" +
                "<name>0</name><family><ethernet-switching><port-mode>trunk</port-mode><vlan>" +
                "<members>Dev_Team_vlan_100</members><members>Finance_Team_vlan_300</members>" +
                "<members>Admin_Team_vlan_200</members></vlan></ethernet-switching></family>" +
                "</unit></interface></interfaces><policy-options><policy-statement><name>My_Intent</name>" +
                "<term><name>vni300</name><from><community>vni300</community></from><then><accept/>" +
                "</then></term><term><name>vni200</name><from><community>vni200</community></from>" +
                "<then><accept/></then></term><term><name>vni100</name><from><community>vni100</community>" +
                "</from><then><accept/></then></term></policy-statement><community><name>vni100</name>" +
                "<members>target:10000:100</members></community><community><name>vni200</name>" +
                "<members>target:10000:200</members></community><community><name>vni300</name>" +
                "<members>target:10000:300</members></community></policy-options><protocols><evpn>" +
                "<vni-options><vni><name>100</name><vrf-target><community>target:10000:100</community>" +
                "</vrf-target></vni><vni><name>200</name><vrf-target><community>target:10000:200</community>" +
                "</vrf-target></vni><vni><name>300</name><vrf-target><community>target:10000:300</community>" +
                "</vrf-target></vni></vni-options><encapsulation>vxlan</encapsulation>" +
                "<extended-vni-list>100</extended-vni-list><extended-vni-list>200</extended-vni-list>" +
                "<extended-vni-list>300</extended-vni-list><multicast-mode>ingress-replication</multicast-mode>" +
                "</evpn></protocols><vlans><vlan><name>Dev_Team_vlan_100</name><vlan-id>100</vlan-id><vxlan>" +
                "<vni>100</vni><ingress-node-replication/></vxlan></vlan><vlan><name>Finance_Team_vlan_300</name>" +
                "<vlan-id>300</vlan-id><vxlan><vni>300</vni><ingress-node-replication/></vxlan></vlan><vlan>" +
                "<name>Admin_Team_vlan_200</name><vlan-id>200</vlan-id><vxlan><vni>200</vni>" +
                "<ingress-node-replication/></vxlan></vlan></vlans></configuration></load-configuration>" +
                "<commit/><unlock-configuration/>";
    }
}
