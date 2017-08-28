/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.opendaylight.nic.rpc.TestUtils;
import org.opendaylight.nic.rpc.model.juniper.rpc.mapping.DeviceDetails;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.when;

/**
 * Created by yrineu on 08/08/17.
 */
@PrepareForTest(ConfigurationFactory.class)
@RunWith(PowerMockRunner.class)
public class ConfigurationFactoryTest {

    @Mock
    private DeviceDetails deviceDetailsMock01;
    @Mock
    private DeviceDetails deviceDetailsMock02;

    @Spy
    private ConfigurationFactory configurationFactory = new ConfigurationFactory();

    @Test
    public void testOrderingConfigsByDeviceDetails() {
        when(deviceDetailsMock01.getHttpIp()).thenReturn("192.168.1.1");

        final Set<Configuration> configurations = TestUtils.generateConfigurationsForTest();
        configurations.forEach(configuration -> configurationFactory.orderConfigurationByDevice(configuration, deviceDetailsMock01, false));

        Map<DeviceDetails, String> commitByDeviceDetails = configurationFactory.generateCommitByDevice();

        Assert.assertNotNull(commitByDeviceDetails);
        Assert.assertNotNull(commitByDeviceDetails.get(deviceDetailsMock01));

    }

    @Test
    public void testOrderingConfigsByDeviceDetailsForInterface() {
        when(deviceDetailsMock01.getHttpIp()).thenReturn("192.168.1.1");
        final Configuration configuration = TestUtils.generateInterfaceConfigurationForTest();

        configurationFactory.orderConfigurationByDevice(configuration, deviceDetailsMock01, false);
        final Map<DeviceDetails, String> commitByDeviceDetails = configurationFactory.generateCommitByDevice();

        Assert.assertNotNull(commitByDeviceDetails.get(deviceDetailsMock01));
    }

    @Test
    public void testOrderingDeleteConfigsByDeviceDetails() {
        when(deviceDetailsMock01.getHttpIp()).thenReturn("192.168.1.1");

        final Set<Configuration> configurations = TestUtils.generateConfigurationsForTest();
        configurations.forEach(configuration -> configurationFactory.orderConfigurationByDevice(configuration, deviceDetailsMock01, true));

        Map<DeviceDetails, String> commitDeleteByDeviceDetails = configurationFactory.generateCommitByDevice();

        System.out.println(commitDeleteByDeviceDetails.get(deviceDetailsMock01));
    }

    @Test(expected = NullPointerException.class)
    public void testOrderingConfigsByDeviceDetailsWithDeviceNull() {
        configurationFactory.orderConfigurationByDevice(null, deviceDetailsMock01, false);
    }
}
