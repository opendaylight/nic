/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration.options;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created by yrineu on 28/07/17.
 */
@PrepareForTest(SwitchOptionsConfig.class)
@RunWith(PowerMockRunner.class)
public class SwitchOptionsConfigTest {

    @Test
    public void testRPCStructure() {
        final StringBuffer result = new StringBuffer();
        final SwitchOptionsConfig switchOptionsConfig = new SwitchOptionsConfig(result);
        final String loopbackSwitch = "10.200.19.63";
        switchOptionsConfig.create(loopbackSwitch);
        switchOptionsConfig.generateRPCStructure();

//        Assert.assertEquals(getDefaultStructure(), switchOptionsConfig.generateRPCStructure());
    }

    private String getDefaultStructure() {
        return "<switch-options><route-distinguisher><rd-type>10.200.19.63:1</rd-type></route-distinguisher>" +
                "<vrf-import>switch_options_comm</vrf-import><vrf-target><community>target:65000:2</community>" +
                "<auto></auto></vrf-target></switch-options>";
    }
}
