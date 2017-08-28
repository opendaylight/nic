/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.rest;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflow.VlanInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflow.VlanInfoBuilder;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

/**
 * Created by yrineu on 03/08/17.
 */
@PrepareForTest(JuniperRestServiceImpl.class)
@RunWith(PowerMockRunner.class)
public class JuniperRestServiceImplTest {

    private static final String ID_1 = "evpn1";
    private static final String ID_2 = "evpn2";
    private static final String HTTP_IP = "192.168.1.1";
    private static final String HTTP_PORT = "3000";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "passw";
    private static final String DEVICE_INTERFACE_1 = "xe-0/0/2";
    private static final String DEVICE_INTERFACE_2 = "xe-0/0/3";
    private static final String DEVICE_INTERFACE_3 = "xe-0/0/4";
    private static final String LOOPBACK_IP = "10.200.19.61";
    private List<VlanInfo> vlanInfoList;

    private JuniperRestServiceImpl service;

    @Mock
    private DataBroker dataBrokerMock;

    @Before
    public void setup() {
        vlanInfoList = Lists.newArrayList();
        final VlanInfoBuilder vlanInfosBuilder = new VlanInfoBuilder();
        vlanInfosBuilder.setVlanId(100L);
        vlanInfosBuilder.setVlanName("admin");
        vlanInfoList.add(vlanInfosBuilder.build());
        service = new JuniperRestServiceImpl(dataBrokerMock);
        service.start();
    }

//    @Test
//    public void test() {
//        final List<EvpnDataflows> evpnDataflows = Lists.newArrayList();
//        final EvpnDataflowBuilder evpnDataflowBuilder = new EvpnDataflowBuilder();
//        evpnDataflowBuilder.setId(ID_1);
//        evpnDataflowBuilder.setInterfaceName(DEVICE_INTERFACE_1);
//        evpnDataflowBuilder.setLoopbackIp(LOOPBACK_IP);
//        evpnDataflowBuilder.setVlanInfos(vlanInfoList);
//        evpnDataflowBuilder.setHttpIp(HTTP_IP);
//        evpnDataflowBuilder.setHttpPort(HTTP_PORT);
//        evpnDataflowBuilder.setUserName(USERNAME);
//        evpnDataflowBuilder.setPassword(PASSWORD);
//
//        evpnDataflows.add(evpnDataflowBuilder.build());
//
//        evpnDataflowBuilder.setInterfaceName(DEVICE_INTERFACE_2);
//        evpnDataflows.add(evpnDataflowBuilder.build());
//
//        evpnDataflowBuilder.setId(ID_2);
//        evpnDataflowBuilder.setInterfaceName(DEVICE_INTERFACE_3);
//        evpnDataflows.add(evpnDataflowBuilder.build());
//
//        service.sendConfiguration(evpnDataflows);
//    }
}
