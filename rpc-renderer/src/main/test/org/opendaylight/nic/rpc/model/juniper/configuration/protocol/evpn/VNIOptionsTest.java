/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.protocol.evpn;

import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Set;

/**
 * Created by yrineu on 20/07/17.
 */
@PrepareForTest(VNIOptions.class)
@RunWith(PowerMockRunner.class)
public class VNIOptionsTest {

    private Set<Integer> vlans;
    private VNIOptions vniOptions;
    private StringBuffer result;

    @Before
    public void setup() {
        result = new StringBuffer();
        vlans = Sets.newHashSet();
        vlans.add(100);
        vniOptions = VNIOptions.create(vlans, result);
    }

    @Test
    public void testVNIExtendedVNIList() {
        vniOptions.generateExtendedVniList();
        Assert.assertEquals(getExtendedVniListStructure(), result.toString());
    }

    @Test
    public void testVNIOptions() {
        vniOptions.generateRPCStructure();
        Assert.assertEquals(getVNIOptionsStructure(), result.toString());
    }

    private String getExtendedVniListStructure() {
        return "<extended-vni-list>100</extended-vni-list>";
    }

    private String getVNIOptionsStructure() {
        return new StringBuffer("<vni-options><vni><name>100</name><vrf-target><community>target:10000:100</community>" +
                "</vrf-target></vni></vni-options>").toString();
    }
}
