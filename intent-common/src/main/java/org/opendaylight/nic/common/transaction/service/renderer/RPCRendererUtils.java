/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.opendaylight.nic.common.transaction.utils.CommonUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711.SwitchInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711._switch.infos.SwitchInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host.info.rev170724.HostInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host.info.rev170724.host.infos.HostInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811._switch._interface.detail.AggregatedVlans;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811._switch._interface.detail.AggregatedVlansBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811._switch._interface.detail.AggregatedVlansKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811._switch._interface.status.SwitchInterfaceDetails;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811._switch._interface.status.SwitchInterfaceDetailsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811._switch.interfaces.status.SwitchInterfaceStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811._switch.interfaces.status.SwitchInterfaceStatusBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.queue.rev170807.evpn.dataflow.queue.EvpnDataflows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.queue.rev170807.evpn.dataflow.queue.EvpnDataflowsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflow.VlanInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflow.VlanInfoBuilder;

import java.util.List;
import java.util.Map;

/**
 * Created by yrineu on 15/08/17.
 */
public class RPCRendererUtils {

    private final CommonUtils commonUtils;

    public RPCRendererUtils(final CommonUtils commonUtils) {
        this.commonUtils = commonUtils;
    }

    public List<EvpnDataflows> extractEvpnDataflows(final List<String> targetVlans) {
        final Map<SwitchInfo, Map<String, List<VlanInfo>>> vlanInfosBySwitch = Maps.newConcurrentMap();

        final HostInfos hostInfos = commonUtils.retrieveHostInfos();
        final SwitchInfos switchInfos = commonUtils.retrieveSwitchInfos();

        targetVlans.forEach(targetVlan -> extractDataflowDetails(targetVlan, vlanInfosBySwitch, hostInfos, switchInfos));
        final List<EvpnDataflows> evpnDataflows = createEvpnDataflows(vlanInfosBySwitch);

        return evpnDataflows;
    }

    private List<EvpnDataflows> createEvpnDataflows(
            final Map<SwitchInfo, Map<String, List<VlanInfo>>> vlanInfosBySwitchAndInterface) {
        final List<EvpnDataflows> evpnDataflows = Lists.newArrayList();
        vlanInfosBySwitchAndInterface.entrySet().forEach(entry -> {
            final SwitchInfo switchInfo = entry.getKey();
            evpnDataflows.add(extractEvpnDataflowFrom(switchInfo, entry.getValue()));
        });
        return evpnDataflows;
    }

    protected void extractDataflowDetails(final String targetVlanName,
                                          final Map<SwitchInfo, Map<String, List<VlanInfo>>> vlanInfosBySwitch,
                                          final HostInfos hostInfos,
                                          final SwitchInfos switchInfos) {
        hostInfos.getHostInfo().forEach(hostInfo -> hostInfo.getHostInterfaces().forEach(hostInterface -> {
            hostInterface.getBridges().forEach(bridges -> {
                final String bridgeVlanNme = bridges.getVlanName().getValue();
                final Long vlanId = bridges.getVlanId().longValue();
                if (bridgeVlanNme.equals(targetVlanName)) {
                    final String switchName = hostInfo.getSwitchName().getValue();
                    extractVlanNameBySwitch(
                            extractSwitchInfoBy(switchName, switchInfos.getSwitchInfo()),
                            targetVlanName,
                            vlanId,
                            hostInterface.getSwitchInterface(),
                            vlanInfosBySwitch);
                }
            });
        }));
    }

    protected void extractVlanNameBySwitch(final SwitchInfo switchInfo,
                                           final String vlanName,
                                           final Long vlanId,
                                           final String interfaceName,
                                           final Map<SwitchInfo, Map<String, List<VlanInfo>>> vlanInfosBySwitch) {
        final VlanInfoBuilder vlanInfoBuilder = new VlanInfoBuilder();
        vlanInfoBuilder.setVlanId(vlanId.longValue());
        vlanInfoBuilder.setVlanName(vlanName);
        if (vlanInfosBySwitch.get(switchInfo) != null) {
            final Map<String, List<VlanInfo>> vlanInfosByInterface = vlanInfosBySwitch.get(switchInfo);
            vlanInfosByInterface.get(interfaceName).add(vlanInfoBuilder.build());
        } else {
            vlanInfosBySwitch.put(switchInfo, Maps.newHashMap());
            vlanInfosBySwitch.get(switchInfo).put(interfaceName, Lists.newArrayList(vlanInfoBuilder.build()));
        }
    }

    protected SwitchInfo extractSwitchInfoBy(final String switchName,
                                             final List<SwitchInfo> switchInfos) {
        SwitchInfo[] result = new SwitchInfo[1];
        switchInfos.forEach(switchInfo -> {
            if (switchInfo.getName().getValue().equals(switchName)) {
                result[0] = switchInfo;
            }
        });
        return result[0];
    }

    private EvpnDataflows extractEvpnDataflowFrom(final SwitchInfo switchInfo,
                                                  final Map<String, List<VlanInfo>> vlanInfosByInterface) {
        final EvpnDataflowsBuilder builder = new EvpnDataflowsBuilder();
        builder.setLoopbackIp(switchInfo.getLoopbackIp());
        builder.setHttpIp(switchInfo.getHttpIp());
        builder.setHttpPort(switchInfo.getHttpPort());
        builder.setUserName(switchInfo.getHttpUser());
        builder.setPassword(switchInfo.getHttpPassword());
        builder.setInterfaceName(vlanInfosByInterface.keySet().iterator().next());
        builder.setVlanInfo(vlanInfosByInterface.values().iterator().next());
        builder.setId(switchInfo.getName().getValue());
        return builder.build();
    }

    public List<AggregatedVlans> extractAggregatedVlans(final List<VlanInfo> vlanInfos) {
        final List<AggregatedVlans> result = Lists.newArrayList();
        final AggregatedVlansBuilder aggregatedVlansBuilder = new AggregatedVlansBuilder();
        vlanInfos.forEach(vlanInfo -> {
            aggregatedVlansBuilder.setVlanId(vlanInfo.getVlanId());
            aggregatedVlansBuilder.setVlanName(vlanInfo.getVlanName());
            aggregatedVlansBuilder.setKey(new AggregatedVlansKey(vlanInfo.getKey().getVlanName()));
            result.add(aggregatedVlansBuilder.build());
        });
        return result;
    }

    public synchronized void handleHostInfoChanges(final List<HostInfo> hostInfosList) {
        final List<SwitchInterfaceStatus> interfaceStatusList =
                extractInterfaceStatusList(groupHostInfosBySwitchAndInterface(hostInfosList));
        commonUtils.updateSwitchInterfaceStatusTree(interfaceStatusList);
    }


    private List<SwitchInterfaceStatus> extractInterfaceStatusList(
            final Map<String, Map<String, List<VlanInfo>>> vlanInfosByInterfaceAndSwitch) {
        final List<SwitchInterfaceStatus> interfaceStatusList = Lists.newArrayList();

        final SwitchInterfaceStatusBuilder interfaceStatusBuilder = new SwitchInterfaceStatusBuilder();
        final SwitchInterfaceDetailsBuilder interfaceDetailsBuilder = new SwitchInterfaceDetailsBuilder();

        vlanInfosByInterfaceAndSwitch.entrySet().forEach(interfacesBySwitch -> {
            final List<SwitchInterfaceDetails> interfaceDetailsList = Lists.newArrayList();
            interfaceStatusBuilder.setSwitchId(interfacesBySwitch.getKey());
            interfaceStatusBuilder.setSwitchInterfaceDetails(Lists.newArrayList());
            interfacesBySwitch.getValue().entrySet().forEach(vlanInfosByInterface -> {
                final String switchInterface = vlanInfosByInterface.getKey();
                if (!interfaceStatusBuilder.getSwitchInterfaceDetails().isEmpty()) {
                    interfaceStatusBuilder.getSwitchInterfaceDetails().forEach(currentInterfaceDetails -> {
                        final String currentSwitchInterface = currentInterfaceDetails.getSwitchInterfaceName();
                        if (currentSwitchInterface.equals(switchInterface)) {
                            interfaceDetailsBuilder.getAggregatedVlans().addAll(currentInterfaceDetails.getAggregatedVlans());
                        }
                    });
                } else {
                    interfaceDetailsBuilder.setSwitchInterfaceName(switchInterface);
                    interfaceDetailsBuilder.setAggregatedVlans(extractAggregatedVlans(vlanInfosByInterface.getValue()));
                }
                interfaceDetailsList.add(interfaceDetailsBuilder.build());
            });
            interfaceStatusBuilder.setSwitchInterfaceDetails(interfaceDetailsList);
            interfaceStatusList.add(interfaceStatusBuilder.build());
        });
        return interfaceStatusList;
    }

    private Map<String, Map<String, List<VlanInfo>>> groupHostInfosBySwitchAndInterface(final List<HostInfo> hostInfoList) {
        final VlanInfoBuilder vlanInfoBuilder = new VlanInfoBuilder();
        final Map<String, Map<String, List<VlanInfo>>> vlanInfosByInterfaceAndSwitch = Maps.newConcurrentMap();
        hostInfoList.forEach(hostInfo -> {
            final String switchName = hostInfo.getSwitchName().getValue();
            if (vlanInfosByInterfaceAndSwitch.get(switchName) == null) {
                vlanInfosByInterfaceAndSwitch.put(switchName, Maps.newConcurrentMap());
            }
            hostInfo.getHostInterfaces().forEach(interfaces -> {
                final List<VlanInfo> vlanInfoList = Lists.newArrayList();
                final String switchInterface = interfaces.getSwitchInterface();
                if (vlanInfosByInterfaceAndSwitch.get(switchName).get(switchInterface) == null) {
                    vlanInfosByInterfaceAndSwitch.get(switchName).put(switchInterface, vlanInfoList);
                }
                interfaces.getBridges().forEach(bridges -> {
                    vlanInfoBuilder.setVlanId(bridges.getVlanId().longValue());
                    vlanInfoBuilder.setVlanName(bridges.getVlanName().getValue());
                    vlanInfosByInterfaceAndSwitch.get(switchName).get(switchInterface).add(vlanInfoBuilder.build());
                });
            });
        });
        return vlanInfosByInterfaceAndSwitch;
    }
}
