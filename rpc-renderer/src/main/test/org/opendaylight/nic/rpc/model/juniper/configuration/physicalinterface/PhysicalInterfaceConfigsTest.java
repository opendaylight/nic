/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.physicalinterface;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Set;

/**
 * Created by yrineu on 28/07/17.
 */
@PrepareForTest(PhysicalInterfaceConfigs.class)
@RunWith(PowerMockRunner.class)
public class PhysicalInterfaceConfigsTest {

    private StringBuffer result;
    @Test
    public void testRpcStructure() {
        result = new StringBuffer();
        final Set<String> vlans = Sets.newHashSet();
        final String interfaceName = "xe-0/0/2";
        vlans.add("Dev_Team");
        final PhysicalInterfaceConfigs configs = PhysicalInterfaceConfigs.create(interfaceName, vlans, result);
        configs.generateRPCStructure();

        Assert.assertEquals(getRpcStructure(), result.toString());
    }

    private String getRpcStructure() {
        return "<interfaces><interface><name>xe-0/0/2</name><description>xe-0/0/2_</description><unit>" +
                "<name>0</name><family><ethernet-switching><vlan><members>Dev_Team</members>" +
                "</vlan></ethernet-switching></family></unit></interface></interfaces>";
    }
}
