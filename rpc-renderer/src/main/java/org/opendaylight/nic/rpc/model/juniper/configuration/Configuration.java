/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.configuration;

import org.opendaylight.nic.rpc.model.juniper.configuration.options.SwitchOptionsConfig;
import org.opendaylight.nic.rpc.model.juniper.configuration.physicalinterface.PhysicalInterfaceConfigs;
import org.opendaylight.nic.rpc.model.juniper.configuration.policy.statement.PolicyActionType;
import org.opendaylight.nic.rpc.model.juniper.configuration.policy.statement.PolicyMatchType;
import org.opendaylight.nic.rpc.model.juniper.configuration.policy.statement.PolicyOptionsConfig;
import org.opendaylight.nic.rpc.model.juniper.configuration.protocol.ProtocolConf;
import org.opendaylight.nic.rpc.model.juniper.configuration.vlan.EvpnEncapsulationType;
import org.opendaylight.nic.rpc.model.juniper.configuration.vlan.VlanConfig;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;

import java.util.Map;
import java.util.Set;

/**
 * This class is responsible to generate configuration schemas for different services
 */
public class Configuration {

    private ProtocolConf protocolConf;
    private PhysicalInterfaceConfigs physicalInterfaceConfigs;
    private PolicyOptionsConfig policyOptionsConfig;
    private VlanConfig vlanConfig;
    private SwitchOptionsConfig switchOptionsConfig;
    private StringBuffer result;

    public Configuration() {
        this.result = new StringBuffer();
    }

    public void configureVxlans(final Map<String, Integer> vlanNameById) {
        this.vlanConfig = VlanConfig.create(vlanNameById, EvpnEncapsulationType.vxlan.name(), result);
    }

    /**
     * Configure a set of VLAN names for a given interface.
     * @param interfaceName the interface name. ie: xe-0/0/0 as {@link String}
     * @param vlanNames a set of VLAN names for that interface. As a {@link Set} of {@link String}
     */
    public void configureInterfaceVlans(final String interfaceName,
                                        final Set<String> vlanNames) {
        this.physicalInterfaceConfigs = PhysicalInterfaceConfigs.create(
                interfaceName,
                vlanNames,
                result);
    }

    /**
     * Configure an IPv4Prefix for a given Interface.
     * @param interfaceName the interface name. ie: xe-0/0/0 as {@link String}
     * @param ipv4Prefix the {@link Ipv4Prefix}
     */
    public void configureInterfaceIp(final String interfaceName,
                                     final Ipv4Prefix ipv4Prefix) {
        this.physicalInterfaceConfigs = PhysicalInterfaceConfigs.create(
                interfaceName,
                ipv4Prefix,
                result);
    }

    /**
     * Configure Evpn for a set of VLAN IDs
     * @param vlans the {@link Set} of VLAN IDs as {@link Integer}.
     */
    public void configureEvpn(final Set<Integer> vlans) {
        if (this.protocolConf == null) {
            this.protocolConf = new ProtocolConf(result);
        }
        this.protocolConf.createEvpnConfigs(vlans);
    }

    /**
     * Configure policies for a given {@link Set} of VLAN IDs as {@link Integer}
     * @param policyName the policy name as {@link String}
     * @param vlans the {@link Set} of VLAN IDs as {@link Integer}.
     */
    public void configurePolicyCommunityAccept(final String policyName,
                                               final Set<Integer> vlans) {
        this.policyOptionsConfig = PolicyOptionsConfig.create(
                policyName,
                vlans,
                PolicyMatchType.community,
                PolicyActionType.accept,
                result);
    }

    public void configureOSPF(final String ospfName,
                              final Map<String, String> interfaceNameByType) {
        if (this.protocolConf == null) {
            this.protocolConf = new ProtocolConf(result);
        }
        this.protocolConf.createOSPFConfigs(ospfName, interfaceNameByType);
    }

    public void configureEvpnBgp(final String name,
                                 final String groupType,
                                 final String localAddress,
                                 final Set<String> neighbors) {
        if (this.protocolConf == null) {
            this.protocolConf = new ProtocolConf(result);
        }
        this.protocolConf.createEvpnBgpConfigs(name, groupType, localAddress, neighbors, result);
    }

    public void configureSwitchOptions(final String loopbackIp) {
        this.switchOptionsConfig = new SwitchOptionsConfig(result);
        switchOptionsConfig.create(loopbackIp);
    }

    /**
     * Generates the switch structure schema as.
     * @return the schema as {@link String}
     */
    public String generateRPCStructure() {
        result.append("<configuration>");

        if (switchOptionsConfig != null) {
            switchOptionsConfig.generateRPCStructure();
        }

        if (physicalInterfaceConfigs != null) {
            physicalInterfaceConfigs.generateRPCStructure();
        }

        if (policyOptionsConfig != null) {
            policyOptionsConfig.generateRPCStructure();
        }

        if (protocolConf != null) {
            protocolConf.generateRPCStructure();
        }

        if (vlanConfig != null) {
            vlanConfig.generateRPCStructure();
        }

        result.append("</configuration>");
        return generateCommitmentConfig(result.toString());
    }

    public String generateCommitCheckStructure() {
        final String toBeChecked = generateRPCStructure();
        return generateCommitCheckConfig(toBeChecked);
    }

    private String generateCommitmentConfig(final String baseConfig) {
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
}
