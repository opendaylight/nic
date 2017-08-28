/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.opendaylight.nic.rpc.model.juniper.rpc.mapping.DeviceDetails;

import java.util.Map;
import java.util.Set;

/**
 * Created by yrineu on 08/08/17.
 */
public class ConfigurationFactory {

    private Map<String, Set<Configuration>> configurationsByIp;
    private Map<String, DeviceDetails> deviceDetailsByIp;
    private Boolean isADeleteSchema = false;

    public ConfigurationFactory() {
        this.configurationsByIp = Maps.newConcurrentMap();
        this.deviceDetailsByIp = Maps.newConcurrentMap();
    }

    public synchronized void orderConfigurationByDevice(final Configuration evpnConfiguration,
                                                        final DeviceDetails deviceDetails,
                                                        final Boolean isADeleteSchema) {
        this.isADeleteSchema = isADeleteSchema;
        Preconditions.checkNotNull(evpnConfiguration);
        Preconditions.checkNotNull(deviceDetails);
        final String ip = deviceDetails.getHttpIp();
        Set<Configuration> configurations = configurationsByIp.get(ip);
        if (configurations == null) {
            configurations = Sets.newConcurrentHashSet();
            configurationsByIp.put(ip, configurations);
        }
        configurations.add(evpnConfiguration);
        deviceDetailsByIp.put(ip, deviceDetails);
    }

    public synchronized Map<DeviceDetails, String> generateCommitByDevice() {
        final Map<DeviceDetails, String> commitByDevice = Maps.newConcurrentMap();
        final Configuration baseConfiguration = new Configuration();
        configurationsByIp.keySet().forEach(ip -> commitByDevice.put(
                deviceDetailsByIp.get(ip),
                baseConfiguration.compileConfigs(configurationsByIp.get(ip), this.isADeleteSchema)));
        return commitByDevice;
    }

    public synchronized void clear() {
        configurationsByIp.clear();
        deviceDetailsByIp.clear();
    }
}
