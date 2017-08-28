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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.nic.common.transaction.exception.RendererServiceException;
import org.opendaylight.nic.common.transaction.service.renderer.RPCRenderer;
import org.opendaylight.nic.common.transaction.utils.CommonUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.evpn.rev170724.intent.evpn.EvpnServices;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.evpn.rev170724.intent.evpns.IntentEvpn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711.SwitchName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711._switch.infos.SwitchInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.bridge.info.rev170731.VlanName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host._interface.rev170731.host._interface.Bridges;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host.info.rev170724.HostInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host.info.rev170724.host.info.HostInterfaces;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host.info.rev170724.host.infos.HostInfo;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created by yrineu on 03/08/17.
 */
@PrepareForTest(RPCRenderer.class)
@RunWith(PowerMockRunner.class)
public class RPCRendererTest {

    @Mock
    private IntentEvpn intentEvpnMock;
    @Mock
    private HostInfos hostInfosMock;
    @Mock
    private DataBroker dataBrokerMock;
    @Mock
    private ReadOnlyTransaction readOnlyTransactionMock;
    @Mock
    private Optional<IntentEvpn> intentEvpnOptionalMock;
    @Mock
    private EvpnServices evpnServicesMock;
    @Mock
    private HostInfo hostInfoMock;
    @Mock
    private SwitchName switchNameMock;
    @Mock
    private SwitchInfo switchInfoMock;
    @Mock
    private HostInterfaces hostInterfacesMock;
    @Mock
    private Bridges bridgesMock;
    @Mock
    private VlanName vlanNameMock;
    @Mock
    private InstanceIdentifier<IntentEvpn> evpnIdentifierMock;

    private List<HostInfo> hostInfoListMock;
    private List<EvpnServices> evpnServiceListMock;
    private List<HostInterfaces> hostInterfacesListMock;
    private List<Bridges> bridgesListMock;

    private static final String INTENT_NAME = "Intent_A";
    private static final String VLAN_NAME = "admin";
    private static final Integer VLAN_ID = 100;
    private static final String SWITCH_NAME = "Switch_Server";
    private static final String HTTP_IP = "192.168.1.1";
    private static final String HTTP_PORT = "3000";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "pass";
    private static final String LOOPBACK_IP = "10.200.10.1";
    private static final String SWITCH_INTERFACE = "xe-0/0/2";

    @Mock
    private CommonUtils commonUtils;

    private RPCRenderer service;

    @Before
    public void setup() throws Exception {

        evpnServiceListMock = Lists.newArrayList();
        hostInfoListMock = Lists.newArrayList();
        hostInterfacesListMock = Lists.newArrayList();
        bridgesListMock = Lists.newArrayList();

        evpnServiceListMock.add(evpnServicesMock);
        hostInfoListMock.add(hostInfoMock);
        hostInterfacesListMock.add(hostInterfacesMock);
        bridgesListMock.add(bridgesMock);

        whenNew(RPCRenderer.class).withArguments(commonUtils).thenReturn(service);
        whenNew(CommonUtils.class).withArguments(dataBrokerMock).thenReturn(commonUtils);
        when(dataBrokerMock.newReadOnlyTransaction()).thenReturn(readOnlyTransactionMock);
        when(commonUtils.retrieveIntentVlans(INTENT_NAME)).thenReturn(intentEvpnMock);
        when(commonUtils.retrieveHostInfos()).thenReturn(hostInfosMock);
        when(intentEvpnOptionalMock.get()).thenReturn(intentEvpnMock);
        when(intentEvpnOptionalMock.isPresent()).thenReturn(true);
//        when(readOnlyTransactionMock.read(LogicalDatastoreType.CONFIGURATION,
//                evpnIdentifierMock).checkedGet()).thenReturn(intentEvpnOptionalMock);
        when(intentEvpnMock.getIntentEvpnName()).thenReturn(INTENT_NAME);
        when(intentEvpnMock.getEvpnServices()).thenReturn(evpnServiceListMock);
        when(evpnServicesMock.getVlanName()).thenReturn(VLAN_NAME);
        when(hostInfosMock.getHostInfo()).thenReturn(hostInfoListMock);
        when(hostInfoMock.getSwitchName()).thenReturn(switchNameMock);
        when(switchNameMock.getValue()).thenReturn(SWITCH_NAME);
        when(commonUtils.retrieveSwitchInfo(SWITCH_NAME)).thenReturn(switchInfoMock);
        when(switchInfoMock.getHttpIp()).thenReturn(HTTP_IP);
        when(switchInfoMock.getHttpPort()).thenReturn(HTTP_PORT);
        when(switchInfoMock.getHttpUser()).thenReturn(USERNAME);
        when(switchInfoMock.getHttpPassword()).thenReturn(PASSWORD);
        when(switchInfoMock.getLoopbackIp()).thenReturn(LOOPBACK_IP);
        when(hostInfoMock.getHostInterfaces()).thenReturn(hostInterfacesListMock);
        when(hostInterfacesMock.getSwitchInterface()).thenReturn(SWITCH_INTERFACE);
        when(hostInterfacesMock.getBridges()).thenReturn(bridgesListMock);
        when(bridgesMock.getVlanName()).thenReturn(vlanNameMock);
        when(vlanNameMock.getValue()).thenReturn(VLAN_NAME);
        when(bridgesMock.getVlanId()).thenReturn(VLAN_ID);

        service = Mockito.spy(new RPCRenderer(commonUtils));
    }

    @Test
    public void test() throws RendererServiceException {
//        service.evaluateAction(INTENT_NAME);
    }
}
