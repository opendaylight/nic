/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.renderer;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.CheckedFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.nic.common.transaction.utils.CommonUtils;
import org.opendaylight.nic.common.transaction.utils.InstanceIdentifierUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711.SwitchInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711.SwitchName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711._switch.infos.SwitchInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.bridge.info.rev170731.BridgeName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.bridge.info.rev170731.VlanName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host._interface.rev170731.InterfaceName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host._interface.rev170731.host._interface.Bridges;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host.info.rev170724.HostInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host.info.rev170724.HostName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host.info.rev170724.host.info.HostInterfaces;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host.info.rev170724.host.infos.HostInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811.SwitchInterfaceDetail;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811._switch._interface.detail.AggregatedVlans;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflow.VlanInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflow.VlanInfoKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by yrineu on 17/08/17.
 */
@RunWith(PowerMockRunner.class)
public class RPCRendererUtilsTest {

    @Mock
    private DataBroker dataBroker;
    private CommonUtils commonUtilsMock;
    private RPCRendererUtils rpcRendererUtils;
    @Mock
    private ReadOnlyTransaction readOnlyTransactionMock;

    private InstanceIdentifier<HostInfos> hostInfosIdentifierMock01;

    @Mock
    private VlanInfo vlanInfoMock01;
    @Mock
    private VlanInfo vlanInfoMock02;
    @Mock
    private VlanInfo vlanInfoMock03;

    @Mock
    private SwitchInfo switchInfoMock01;
    @Mock
    private SwitchInfo switchInfoMock02;
    @Mock
    private SwitchInfo switchInfoMock03;

    @Mock
    private SwitchName switchNameMock01;
    @Mock
    private SwitchName switchNameMock02;
    @Mock
    private SwitchName switchNameMock03;

    @Mock
    private HostInfos hostInfosMock01;
    @Mock
    private HostInfos hostInfosMock02;
    @Mock
    private HostInfos hostInfosMock03;

    @Mock
    private HostInfo hostInfoMock01;
    @Mock
    private HostInfo hostInfoMock02;
    @Mock
    private HostInfo hostInfoMock03;

    @Mock
    private Optional<HostInfos> hostInfosOptionalMock01;
    @Mock
    private Optional<HostInfos> hostInfosOptionalMock02;
    @Mock
    private Optional<HostInfos> hostInfosOptionalMock03;

    @Mock
    private CheckedFuture<Optional<HostInfos>, ReadFailedException> hostInfosCheckedFeature01;

    @Mock
    private SwitchInfos switchInfos01;

    @Mock
    private SwitchInterfaceDetail interfaceDetailMock01;
    @Mock
    private SwitchInterfaceDetail interfaceDetailMock02;
    @Mock
    private SwitchInterfaceDetail interfaceDetailMock03;

    @Mock
    private AggregatedVlans aggregatedVlanMock01;
    @Mock
    private AggregatedVlans aggregatedVlanMock02;
    @Mock
    private AggregatedVlans aggregatedVlanMock03;

    @Mock
    private Bridges bridgesMock01;
    @Mock
    private Bridges bridgesMock02;
    @Mock
    private Bridges bridgesMock03;

    @Mock
    private VlanName vlanNameMock01;
    @Mock
    private VlanName vlanNameMock02;
    @Mock
    private VlanName vlanNameMock03;

    @Mock
    private BridgeName bridgeNameMock01;
    @Mock
    private BridgeName bridgeNameMock02;
    @Mock
    private BridgeName bridgeNameMock03;

    @Mock
    private HostInterfaces hostInterfacesMock01;
    @Mock
    private HostInterfaces hostInterfacesMock02;
    @Mock
    private HostInterfaces hostInterfacesMock03;

    @Mock
    private InterfaceName hostInterfaceName01;
    @Mock
    private InterfaceName hostInterfaceName02;
    @Mock
    private InterfaceName hostInterfaceName03;

    @Mock
    private HostName hostNameMock01;
    @Mock
    private HostName hostNameMock02;
    @Mock
    private HostName hostNameMock03;

    private static final String VLAN_NAME_100 = "Vlan100";
    private static final String VLAN_NAME_200 = "Vlan200";
    private static final String VLAN_NAME_300 = "Vlan300";

    private static final String INTERFACE_NAME01 = "xe-0/0/1";
    private static final String INTERFACE_NAME02 = "xe-0/0/1";
    private static final String INTERFACE_NAME03 = "xe-0/0/1";

    private static final String HOST_INTERFACE_NAME_01 = "eth1";
    private static final String HOST_INTERFACE_NAME_02 = "eth2";
    private static final String HOST_INTERFACE_NAME_03 = "eth3";

    private static final String BRIDGE_NAME_01 = "eth1.100";
    private static final String BRIDGE_NAME_02 = "eth2.200";
    private static final String BRIDGE_NAME_03 = "eth3.300";

    private static final Long VLAN_ID_100 = 100L;
    private static final Long VLAN_ID_200 = 200L;
    private static final Long VLAN_ID_300 = 300L;

    private static final String SWITCH_NAME_01 = "SW01";
    private static final String SWITCH_NAME_02 = "SW02";
    private static final String SWITCH_NAME_03 = "SW03";

    private static final String HOST_NAME_01 = "Host01";
    private static final String HOST_NAME_02 = "Host02";
    private static final String HOST_NAME_03 = "Host03";

    private final List<HostInfo> hostInfoList01 = Lists.newArrayList();
    private final List<HostInfo> hostInfoList02 = Lists.newArrayList();
    private final List<HostInfo> hostInfoList03 = Lists.newArrayList();

    private final List<SwitchInfo> switchInfoList01 = Lists.newArrayList();
    private final List<SwitchInfo> switchInfoList02 = Lists.newArrayList();
    private final List<SwitchInfo> switchInfoList03 = Lists.newArrayList();

    private final List<AggregatedVlans> aggregatedVlanList01 = Lists.newArrayList();
    private final List<AggregatedVlans> aggregatedVlanList02 = Lists.newArrayList();
    private final List<AggregatedVlans> aggregatedVlanList03 = Lists.newArrayList();

    private final List<SwitchInterfaceDetail> switchInterfaceDetailList01 = Lists.newArrayList();
    private final List<SwitchInterfaceDetail> switchInterfaceDetailList02 = Lists.newArrayList();
    private final List<SwitchInterfaceDetail> switchInterfaceDetailList03 = Lists.newArrayList();

    private final List<Bridges> bridgesList01 = Lists.newArrayList();
    private final List<Bridges> bridgesList02 = Lists.newArrayList();
    private final List<Bridges> bridgesList03 = Lists.newArrayList();

    private final List<HostInterfaces> hostInterfacesList01 = Lists.newArrayList();
    private final List<HostInterfaces> hostInterfacesList02 = Lists.newArrayList();
    private final List<HostInterfaces> hostInterfacesList03 = Lists.newArrayList();

    private final List<VlanInfo> vlanInfoList = Lists.newArrayList();

    @Before
    public void setup() throws Exception {

        commonUtilsMock = spy(new CommonUtils(dataBroker));
        rpcRendererUtils = new RPCRendererUtils(commonUtilsMock);

        hostInfosIdentifierMock01 = spy(InstanceIdentifierUtils.HOST_INFOS_IDENTIFIER.builder().build());

        hostInfoList01.add(hostInfoMock01);
        hostInfoList01.add(hostInfoMock02);
        hostInfoList02.add(hostInfoMock03);
        hostInfoList03.add(hostInfoMock03);

        switchInfoList01.add(switchInfoMock01);
        switchInfoList02.add(switchInfoMock02);
        switchInfoList03.add(switchInfoMock03);

        aggregatedVlanList01.add(aggregatedVlanMock01);
        aggregatedVlanList01.add(aggregatedVlanMock02);
        aggregatedVlanList02.add(aggregatedVlanMock03);

        switchInterfaceDetailList01.add(interfaceDetailMock01);
        switchInterfaceDetailList01.add(interfaceDetailMock02);
        switchInterfaceDetailList02.add(interfaceDetailMock03);

        bridgesList01.add(bridgesMock01);
        bridgesList02.add(bridgesMock02);
        bridgesList03.add(bridgesMock03);

        hostInterfacesList01.add(hostInterfacesMock01);
        hostInterfacesList02.add(hostInterfacesMock02);
        hostInterfacesList03.add(hostInterfacesMock03);

        vlanInfoList.add(vlanInfoMock01);
        vlanInfoList.add(vlanInfoMock02);

        when(hostInfosOptionalMock01.isPresent()).thenReturn(true);
        when(hostInfosOptionalMock02.isPresent()).thenReturn(true);
        when(hostInfosOptionalMock03.isPresent()).thenReturn(true);

        when(hostInfosOptionalMock01.get()).thenReturn(hostInfosMock01);
        when(hostInfosOptionalMock02.get()).thenReturn(hostInfosMock02);
        when(hostInfosOptionalMock03.get()).thenReturn(hostInfosMock03);

        when(dataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTransactionMock);
        when(readOnlyTransactionMock.read(LogicalDatastoreType.CONFIGURATION,
                hostInfosIdentifierMock01)).thenReturn(hostInfosCheckedFeature01);

        when(hostInfosCheckedFeature01.checkedGet()).thenReturn(hostInfosOptionalMock01);

        when(commonUtilsMock.retrieveHostInfos()).thenReturn(hostInfosMock01);
        when(commonUtilsMock.retrieveSwitchInfos()).thenReturn(switchInfos01);

        when(vlanInfoMock01.getVlanId()).thenReturn(VLAN_ID_100);
        when(vlanInfoMock02.getVlanId()).thenReturn(VLAN_ID_200);
        when(vlanInfoMock03.getVlanId()).thenReturn(VLAN_ID_300);

        when(vlanInfoMock01.getVlanName()).thenReturn(VLAN_NAME_100);
        when(vlanInfoMock02.getVlanName()).thenReturn(VLAN_NAME_200);
        when(vlanInfoMock03.getVlanName()).thenReturn(VLAN_NAME_300);

        when(vlanInfoMock01.getKey()).thenReturn(new VlanInfoKey(VLAN_NAME_100));
        when(vlanInfoMock02.getKey()).thenReturn(new VlanInfoKey(VLAN_NAME_200));
        when(vlanInfoMock03.getKey()).thenReturn(new VlanInfoKey(VLAN_NAME_300));

        when(interfaceDetailMock01.getSwitchInterfaceName()).thenReturn(INTERFACE_NAME01);
        when(interfaceDetailMock02.getSwitchInterfaceName()).thenReturn(INTERFACE_NAME02);
        when(interfaceDetailMock03.getSwitchInterfaceName()).thenReturn(INTERFACE_NAME03);

        when(interfaceDetailMock01.getAggregatedVlans()).thenReturn(aggregatedVlanList01);

        //Switch Info mock
        when(switchInfoMock01.getLoopbackIp()).thenReturn("10.0.0.1");
        when(switchInfoMock01.getHttpPassword()).thenReturn("pass");
        when(switchInfoMock01.getHttpUser()).thenReturn("user");
        when(switchInfoMock01.getHttpIp()).thenReturn("192.168.1.1");
        when(switchInfoMock01.getHttpPort()).thenReturn("3001");
        when(switchNameMock01.getValue()).thenReturn(SWITCH_NAME_01);
        when(switchInfoMock01.getName()).thenReturn(switchNameMock01);

        when(switchInfoMock02.getLoopbackIp()).thenReturn("10.0.0.2");
        when(switchInfoMock02.getHttpPassword()).thenReturn("pass");
        when(switchInfoMock02.getHttpUser()).thenReturn("user");
        when(switchInfoMock02.getHttpIp()).thenReturn("192.168.1.1");
        when(switchInfoMock02.getHttpPort()).thenReturn("3002");
        when(switchNameMock02.getValue()).thenReturn(SWITCH_NAME_02);
        when(switchInfoMock02.getName()).thenReturn(switchNameMock02);

        when(switchInfoMock03.getLoopbackIp()).thenReturn("10.0.0.3");
        when(switchInfoMock03.getHttpPassword()).thenReturn("pass");
        when(switchInfoMock03.getHttpUser()).thenReturn("user");
        when(switchInfoMock03.getHttpIp()).thenReturn("192.168.1.1");
        when(switchInfoMock03.getHttpPort()).thenReturn("3003");
        when(switchNameMock03.getValue()).thenReturn(SWITCH_NAME_03);
        when(switchInfoMock03.getName()).thenReturn(switchNameMock03);

        when(hostNameMock01.getValue()).thenReturn(HOST_NAME_01);
        when(hostNameMock02.getValue()).thenReturn(HOST_NAME_02);
        when(hostNameMock03.getValue()).thenReturn(HOST_NAME_03);

        when(hostInfoMock01.getHostName()).thenReturn(hostNameMock01);
        when(hostInfoMock02.getHostName()).thenReturn(hostNameMock02);
        when(hostInfoMock03.getHostName()).thenReturn(hostNameMock03);

        when(hostInfoMock01.getSwitchName()).thenReturn(switchNameMock01);
        when(hostInfoMock02.getSwitchName()).thenReturn(switchNameMock01);
        when(hostInfoMock03.getSwitchName()).thenReturn(switchNameMock01);

        when(hostInfoMock01.getHostInterfaces()).thenReturn(hostInterfacesList01);
        when(hostInfoMock02.getHostInterfaces()).thenReturn(hostInterfacesList02);
        when(hostInfoMock03.getHostInterfaces()).thenReturn(hostInterfacesList03);

        when(hostInfosMock01.getHostInfo()).thenReturn(hostInfoList01);
        when(hostInfosMock02.getHostInfo()).thenReturn(hostInfoList02);
        when(hostInfosMock03.getHostInfo()).thenReturn(hostInfoList03);

        when(switchInfos01.getSwitchInfo()).thenReturn(switchInfoList01);

        when(bridgesMock01.getVlanId()).thenReturn(VLAN_ID_100.intValue());
        when(bridgesMock02.getVlanId()).thenReturn(VLAN_ID_200.intValue());
        when(bridgesMock03.getVlanId()).thenReturn(VLAN_ID_100.intValue());

        when(vlanNameMock01.getValue()).thenReturn(VLAN_NAME_100);
        when(vlanNameMock02.getValue()).thenReturn(VLAN_NAME_200);
        when(vlanNameMock03.getValue()).thenReturn(VLAN_NAME_300);

        when(bridgesMock01.getVlanName()).thenReturn(vlanNameMock01);
        when(bridgesMock02.getVlanName()).thenReturn(vlanNameMock02);
        when(bridgesMock03.getVlanName()).thenReturn(vlanNameMock03);

        when(bridgeNameMock01.getValue()).thenReturn(BRIDGE_NAME_01);
        when(bridgeNameMock02.getValue()).thenReturn(BRIDGE_NAME_02);
        when(bridgeNameMock03.getValue()).thenReturn(BRIDGE_NAME_03);

        when(bridgesMock01.getBridgeName()).thenReturn(bridgeNameMock01);
        when(bridgesMock02.getBridgeName()).thenReturn(bridgeNameMock02);
        when(bridgesMock03.getBridgeName()).thenReturn(bridgeNameMock03);

        when(hostInterfacesMock01.getBridges()).thenReturn(bridgesList01);
        when(hostInterfacesMock02.getBridges()).thenReturn(bridgesList02);

        when(hostInterfacesMock01.getSwitchInterface()).thenReturn(INTERFACE_NAME01);
        when(hostInterfacesMock02.getSwitchInterface()).thenReturn(INTERFACE_NAME02);

        when(hostInterfaceName01.getValue()).thenReturn(HOST_INTERFACE_NAME_01);
        when(hostInterfaceName02.getValue()).thenReturn(HOST_INTERFACE_NAME_02);
        when(hostInterfaceName03.getValue()).thenReturn(HOST_INTERFACE_NAME_03);

        when(hostInterfacesMock01.getInterfaceName()).thenReturn(hostInterfaceName01);
        when(hostInterfacesMock02.getInterfaceName()).thenReturn(hostInterfaceName02);
        when(hostInterfacesMock03.getInterfaceName()).thenReturn(hostInterfaceName03);

        when(aggregatedVlanMock01.getVlanId()).thenReturn(VLAN_ID_100);
        when(aggregatedVlanMock02.getVlanId()).thenReturn(VLAN_ID_200);
        when(aggregatedVlanMock03.getVlanId()).thenReturn(VLAN_ID_300);

        when(aggregatedVlanMock01.getVlanName()).thenReturn(VLAN_NAME_100);
        when(aggregatedVlanMock02.getVlanName()).thenReturn(VLAN_NAME_200);
        when(aggregatedVlanMock03.getVlanName()).thenReturn(VLAN_NAME_300);
    }

    @Ignore
    @Test
    public void testExtractAggregatedVlans() {
        final List<AggregatedVlans> result = rpcRendererUtils.extractAggregatedVlans(vlanInfoList);

        Assert.assertEquals(2, result.size());
    }

    @Ignore
    @Test
    public void testExtractSwitchInfoByWithValidSwitchInfos() {
        final List<SwitchInfo> switchInfos = Lists.newArrayList();
        switchInfos.add(switchInfoMock01);
        switchInfos.add(switchInfoMock02);
        final SwitchInfo result01 = rpcRendererUtils.extractSwitchInfoBy("SW01", switchInfos);
        Assert.assertEquals(result01.getHttpIp(), "192.168.1.1");

        final SwitchInfo result02 = rpcRendererUtils.extractSwitchInfoBy("SW02", switchInfos);
        Assert.assertEquals(result02.getHttpIp(), "192.168.1.2");
    }

    @Ignore
    @Test
    public void testExtractVlanNameBySwitch() {
        final Map<SwitchInfo, Map<String, List<VlanInfo>>> vlanInfosBySwitch = Maps.newConcurrentMap();

        rpcRendererUtils.extractVlanNameBySwitch(
                switchInfoMock01,
                VLAN_NAME_100,
                VLAN_ID_100,
                INTERFACE_NAME01,
                vlanInfosBySwitch);
        Assert.assertFalse(vlanInfosBySwitch.isEmpty());

        rpcRendererUtils.extractVlanNameBySwitch(
                switchInfoMock01,
                VLAN_NAME_200,
                VLAN_ID_200,
                INTERFACE_NAME01,
                vlanInfosBySwitch);
        Assert.assertFalse(vlanInfosBySwitch.get(switchInfoMock01).isEmpty());
        Assert.assertFalse(vlanInfosBySwitch.get(switchInfoMock01).get(INTERFACE_NAME01).isEmpty());
        Assert.assertEquals(vlanInfosBySwitch.get(switchInfoMock01).get(INTERFACE_NAME01).get(0).getVlanId(), VLAN_ID_100);
        Assert.assertEquals(vlanInfosBySwitch.get(switchInfoMock01).get(INTERFACE_NAME01).get(1).getVlanId(), VLAN_ID_200);
    }

    @Ignore
    @Test
    public void testExtractDataflowDetails() {
        final Map<SwitchInfo, Map<String, List<VlanInfo>>> vlanInfosBySwitch = Maps.newConcurrentMap();

        rpcRendererUtils.extractDataflowDetails(
                VLAN_NAME_100,
                vlanInfosBySwitch,
                hostInfosMock01,
                switchInfos01);

        Assert.assertFalse(vlanInfosBySwitch.isEmpty());
    }

    @Ignore
    @Test
    public void testExtractEvpnDataflows() {

        final List<String> targetNames = Lists.newArrayList();
        targetNames.add(VLAN_NAME_100);
        targetNames.add(VLAN_NAME_200);
        targetNames.add(VLAN_NAME_300);

        rpcRendererUtils.extractEvpnDataflows(targetNames);
    }
}
