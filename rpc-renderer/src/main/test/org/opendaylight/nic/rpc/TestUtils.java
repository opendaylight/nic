/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.opendaylight.nic.rpc.model.juniper.configuration.Configuration;

import java.util.Map;
import java.util.Set;

/**
 * Created by yrineu on 08/08/17.
 */
public class TestUtils {

    public static Set<Configuration> generateConfigurationsForTest() {
        final String intentName = "My Intent";

        final String devTeamVlanName = "Dev Team vlan 100";
        final String adminTeamVlanName = "Admin Team vlan 200";

        final Integer vlanDev = 100;
        final Integer vlanAdmin = 200;

        final String interfaceName = "xe-0/0/0";
        final String loopbackIp = "10.200.19.63";

        final Map<String, Integer> vlanNameById01 = Maps.newHashMap();
        final Map<String, Integer> vlanNameById02 = Maps.newHashMap();
        vlanNameById01.put(devTeamVlanName, vlanDev);
        vlanNameById02.put(adminTeamVlanName, vlanAdmin);

        final Set<String> vlansNames01 = vlanNameById01.keySet();
        final Set<Integer> vlanIds01 = Sets.newHashSet(vlanNameById01.values());

        final Set<String> vlansNames02 = vlanNameById02.keySet();
        final Set<Integer> vlanIds02 = Sets.newHashSet(vlanNameById02.values());

        Configuration configuration01 = new Configuration();
        configuration01.configureVxlans(vlanNameById01);
        configuration01.configurePolicyCommunityAccept(vlanIds01);
        configuration01.configureInterfaceVlans(interfaceName, vlansNames01);
        configuration01.configureEvpn(vlanIds01);
        configuration01.configureSwitchOptions(loopbackIp);

        Configuration configuration02 = new Configuration();
        configuration02.configureVxlans(vlanNameById02);
        configuration02.configurePolicyCommunityAccept(vlanIds02);
        configuration02.configureInterfaceVlans(interfaceName, vlansNames02);
        configuration02.configureEvpn(vlanIds02);
        configuration02.configureSwitchOptions(loopbackIp);

        Set<Configuration> configurations = Sets.newHashSet();
        configurations.add(configuration01);
        configurations.add(configuration02);
        return configurations;
    }

    public static Configuration generateInterfaceConfigurationForTest() {

        final String vlanDev = "DEV";
        final String vlanAdmin = "ADMIN";
        final String switchInterface = "xe-0/0/1";

        final Set<String> vlans = Sets.newHashSet();
        vlans.add(vlanDev);
        vlans.add(vlanAdmin);

        final Configuration configuration = new Configuration();
        configuration.configureInterfaceVlans(switchInterface, vlans);

        return configuration;
    }
}
