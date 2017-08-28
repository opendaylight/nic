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
import org.opendaylight.nic.rpc.model.juniper.configuration.options.SwitchOptionsConfig;
import org.opendaylight.nic.rpc.model.juniper.configuration.physicalinterface.PhysicalInterfaceConfigs;
import org.opendaylight.nic.rpc.model.juniper.configuration.policy.statement.PolicyActionType;
import org.opendaylight.nic.rpc.model.juniper.configuration.policy.statement.PolicyMatchType;
import org.opendaylight.nic.rpc.model.juniper.configuration.policy.statement.PolicyOptionsConfig;
import org.opendaylight.nic.rpc.model.juniper.configuration.protocol.ProtocolConf;
import org.opendaylight.nic.rpc.model.juniper.configuration.vlan.EvpnEncapsulationType;
import org.opendaylight.nic.rpc.model.juniper.configuration.vlan.VlanConfig;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This class is responsible to generate configuration schemas for different services
 */
public class Configuration {
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

    private ProtocolConf protocolConf;
    private PhysicalInterfaceConfigs physicalInterfaceConfigs;
    private PolicyOptionsConfig policyOptionsConfig;
    private VlanConfig vlanConfig;
    private SwitchOptionsConfig switchOptionsConfig;
    private StringBuffer result;
    private Boolean isADeleteSchema = false;

    public Configuration() {
        this.result = new StringBuffer();
    }

    public synchronized void configureVxlans(final Map<String, Integer> vlanNameById) {
        this.vlanConfig = VlanConfig.create(vlanNameById, EvpnEncapsulationType.vxlan.name(), result);
    }

    /**
     * Configure a set of VLAN names for a given interface.
     *
     * @param interfaceName the interface name. ie: xe-0/0/0 as {@link String}
     * @param vlanNames     a set of VLAN names for that interface. As a {@link Set} of {@link String}
     */
    public synchronized void configureInterfaceVlans(final String interfaceName,
                                                     final Set<String> vlanNames) {
        this.physicalInterfaceConfigs = PhysicalInterfaceConfigs.create(
                interfaceName,
                vlanNames,
                result);
    }

    /**
     * Configure an IPv4Prefix for a given Interface.
     *
     * @param interfaceName the interface name. ie: xe-0/0/0 as {@link String}
     * @param ipv4Prefix    the {@link Ipv4Prefix}
     */
    public synchronized void configureInterfaceIp(final String interfaceName,
                                                  final Ipv4Prefix ipv4Prefix) {
        this.physicalInterfaceConfigs = PhysicalInterfaceConfigs.create(
                interfaceName,
                ipv4Prefix,
                result);
    }

    /**
     * Configure Evpn for a set of VLAN IDs
     *
     * @param vlans the {@link Set} of VLAN IDs as {@link Integer}.
     */
    public synchronized void configureEvpn(final Set<Integer> vlans) {
        if (this.protocolConf == null) {
            this.protocolConf = new ProtocolConf(result);
        }
        this.protocolConf.createEvpnConfigs(vlans);
    }

    /**
     * Configure policies for a given {@link Set} of VLAN IDs as {@link Integer}
     *
     * @param vlans the {@link Set} of VLAN IDs as {@link Integer}.
     */
    public synchronized void configurePolicyCommunityAccept(final Set<Integer> vlans) {
        this.policyOptionsConfig = PolicyOptionsConfig.create(
                vlans,
                PolicyMatchType.community,
                PolicyActionType.accept,
                result);
    }

    public synchronized void configureOSPF(final String ospfName,
                                           final Map<String, String> interfaceNameByType) {
        if (this.protocolConf == null) {
            this.protocolConf = new ProtocolConf(result);
        }
        this.protocolConf.createOSPFConfigs(ospfName, interfaceNameByType);
    }

    public synchronized void configureEvpnBgp(final String name,
                                              final String groupType,
                                              final String localAddress,
                                              final Set<String> neighbors) {
        if (this.protocolConf == null) {
            this.protocolConf = new ProtocolConf(result);
        }
        this.protocolConf.createEvpnBgpConfigs(name, groupType, localAddress, neighbors, result);
    }

    public synchronized void configureSwitchOptions(final String loopbackIp) {
        this.switchOptionsConfig = new SwitchOptionsConfig(result);
        switchOptionsConfig.create(loopbackIp);
    }

    /**
     * Generates the switch structure schema as.
     *
     * @return the schema as {@link String}
     */
    public synchronized String generateRPCStructure() {
        result.append("<configuration>");
        if (!isNull(switchOptionsConfig) ||
                !isNull(policyOptionsConfig) ||
                !isNull(protocolConf) ||
                !isNull(vlanConfig)) {
            generateStructure(switchOptionsConfig);
            generateStructure(policyOptionsConfig);
            generateStructure(protocolConf);
            generateStructure(vlanConfig);
        }
        if (!isNull(physicalInterfaceConfigs)) {
            generateInterfaceCleanup();
            generateStructure(physicalInterfaceConfigs);
        }
        result.append("</configuration>");
        return result.toString();
    }

    private boolean isNull(final ConfigurationInterface config) {
        return (config == null);
    }

    public synchronized String generateDeleteRPCStructure() {
        this.isADeleteSchema = true;
        return generateRPCStructure();
    }

    private void generateStructure(final ConfigurationInterface config) {
        if (config != null) {
            if (!isADeleteSchema) {
                config.generateRPCStructure();
            } else {
                config.generateDeleteRPCStructure();
            }
        }
    }

    public String generateCommitCheckStructure() {
        final String toBeChecked = generateRPCStructure();
        return generateCommitCheckConfig(toBeChecked);
    }

    public String generateCommitmentConfig(final String baseConfig) {
        final StringBuffer result = new StringBuffer();
        result.append("<lock-configuration/>");
        result.append("<load-configuration>");
        result.append(baseConfig);
        result.append("</load-configuration>");
        result.append("<commit/>");
        result.append("<unlock-configuration/>");
        return result.toString();
    }

    private String generateCommitCheckConfig(final String baseConfig) {
        final StringBuffer result = new StringBuffer();
        result.append("<lock-configuration/>");
        result.append("<load-configuration>");
        result.append(baseConfig);
        result.append("</load-configuration>");
        result.append("<commit-configuration>");
        result.append("<check/>");
        result.append("</commit-configuration>");
        result.append("<unlock-configuration/>");
        return result.toString();
    }

    public synchronized String compileConfigs(final Set<Configuration> configurations,
                                              final Boolean isADeleteSchema) {
        Configuration compiledConfig;
        if (shouldCompile(configurations)) {
            compiledConfig = compile(configurations);
        } else {
            compiledConfig = configurations.iterator().next();
        }
        return generateCommitmentConfig(
                isADeleteSchema ?
                        compiledConfig.generateDeleteRPCStructure() : compiledConfig.generateRPCStructure());
    }

    private boolean shouldCompile(final Set<Configuration> configurations) {
        Boolean result = false;
        final Iterator<Configuration> iterator = configurations.iterator();
        do {
            final Configuration configuration = iterator.next();
            if (!isNull(configuration.vlanConfig) && isNull(configuration.physicalInterfaceConfigs)) {
                result = true;
                break;
            }
        } while (iterator.hasNext());
        return result;
    }

    private synchronized Configuration compile(final Set<Configuration> configurations) {
        final Configuration baseConfig = configurations.iterator().next();
        final Configuration configurationResult = new Configuration();
        final Set<String> vlanNames = Sets.newHashSet(baseConfig.vlanConfig.getVlanNames());
        final Map<String, Integer> vlanNameById = Maps.newHashMap(baseConfig.vlanConfig.getVlanNameById());
        final Set<Integer> vlanIds = Sets.newHashSet(baseConfig.vlanConfig.getVlanIds());
        final Iterator<Configuration> iterator = configurations.iterator();
        do {
            final Configuration configuration = iterator.next();
            vlanNameById.putAll(configuration.vlanConfig.getVlanNameById());
            vlanNames.addAll(configuration.vlanConfig.getVlanNames());
            vlanIds.addAll(configuration.vlanConfig.getVlanIds());
            iterator.remove();
        } while (iterator.hasNext());
        configurationResult.configureVxlans(vlanNameById);
        configurationResult.configurePolicyCommunityAccept(vlanIds);
        configurationResult.configureEvpn(vlanIds);
        configurationResult.configureSwitchOptions(baseConfig.switchOptionsConfig.getLoopbackIp());
        return baseConfig;
    }


    public synchronized void generateInterfaceCleanup() {
        configureInterfaceVlans(
                this.physicalInterfaceConfigs.getInterfaceName(),
                this.physicalInterfaceConfigs.getAggregatedVlans());
        this.physicalInterfaceConfigs.createCleanup();
    }
}
