/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.nic.of.renderer.utils.MatchUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by yrineu on 08/08/16.
 */
//TODO: Create new tests for exception cases and apply refactoring if needed
@PrepareForTest({MatchUtils.class})
@RunWith(PowerMockRunner.class)
public class PortFlowTest {
    @Mock
    private PortFlow portFlow;

    @Mock
    private Integer maxPortNumber;
    @Mock
    private Integer minPortNumber;
    @Mock
    private MatchBuilder matchBuilderMock;
    @Mock
    private Intent intentMock;

    private List<String> endPointGroups;
    private final String srcEndPointGoroup = "openflow:1";
    private final String dstEndPoingGroup = "openflow:2";

    private final String protocolIcmpName = "ProtocolIcmp";
    private final String protocolTcpName = "ProtocolTcp";
    private final String protocolUdpName = "ProtocolUdp";

    private final String ethernetIPV4Name = "EthertypeV4";
    private final String ethernetIPV6Name = "EthertypeV6";

    private final String directionEgressName = "DirectionEgress";
    private final String directionIngressName = "DirectionIngress";

    @Before
    public void setUp() {
        Mockito.when(MatchUtils.createICMPv4Match(matchBuilderMock,
                minPortNumber.shortValue(), maxPortNumber.shortValue())).thenReturn(matchBuilderMock);

        endPointGroups = Arrays.asList(srcEndPointGoroup, dstEndPoingGroup);
        portFlow = new PortFlow(
                maxPortNumber,
                minPortNumber,
                protocolIcmpName,
                ethernetIPV4Name,
                directionEgressName,
                endPointGroups);
    }

    @Test
    public void testCreatePortRangeMatchBuilder() throws Exception {
        Set<MatchBuilder> result = portFlow.createPortRangeMatchBuilder();

        PowerMockito.spy(portFlow);
        Assert.assertNotNull(result);
        PowerMockito.verifyPrivate(portFlow).invoke("getIcmpMatchBuilder");
    }

    @Test
    public void testCreatePortMatchBuilderWithTcpProtocol() throws Exception {
        portFlow = new PortFlow(
                maxPortNumber,
                minPortNumber,
                protocolTcpName,
                ethernetIPV4Name,
                directionEgressName,
                endPointGroups);
        Set<MatchBuilder> result = portFlow.createPortRangeMatchBuilder();

        Assert.assertNotNull(result);
    }

    @Test
    public void testCreatePortMatchBuilderWithUdppProtocol() throws Exception {
        portFlow = new PortFlow(
                maxPortNumber,
                minPortNumber,
                protocolUdpName,
                ethernetIPV4Name,
                directionEgressName,
                endPointGroups);
        Set<MatchBuilder> result = portFlow.createPortRangeMatchBuilder();

        Assert.assertNotNull(result);
    }

    @Test
    public void testCreatePortMatchBuilderWithTcpProtocolIngress() throws Exception {
        portFlow = new PortFlow(
                maxPortNumber,
                minPortNumber,
                protocolTcpName,
                ethernetIPV4Name,
                directionIngressName,
                endPointGroups);
        Set<MatchBuilder> result = portFlow.createPortRangeMatchBuilder();

        Assert.assertNotNull(result);
    }

    @Test
    public void testCreatePortMatchBuilderWithTcpProtocolIngressIpV6() throws Exception {
        portFlow = new PortFlow(
                maxPortNumber,
                minPortNumber,
                protocolTcpName,
                ethernetIPV6Name,
                directionIngressName,
                endPointGroups);
        Set<MatchBuilder> result = portFlow.createPortRangeMatchBuilder();

        Assert.assertNotNull(result);
    }

    @Test
    public void testCreatePortMatchBuilderWithICMPProtocolIngressIpV6() throws Exception {
        portFlow = new PortFlow(
                maxPortNumber,
                minPortNumber,
                protocolIcmpName,
                ethernetIPV6Name,
                directionIngressName,
                endPointGroups);
        Set<MatchBuilder> result = portFlow.createPortRangeMatchBuilder();

        Assert.assertNotNull(result);
    }

    @Test
    public void testCreatePortMatchBuilderWithICMPProtocolEgressIpV6() throws Exception {
        portFlow = new PortFlow(
                maxPortNumber,
                minPortNumber,
                protocolIcmpName,
                ethernetIPV6Name,
                directionEgressName,
                endPointGroups);
        Set<MatchBuilder> result = portFlow.createPortRangeMatchBuilder();

        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateFlowName() {
        String uuid = UUID.randomUUID().toString();
        final String expected = "L2_Rule_openflow:1openflow:2Uuid [_value="+uuid+"]icmp0_0";
        Mockito.when(intentMock.getId()).thenReturn(Uuid.getDefaultInstance(uuid));

        String result = portFlow.createFlowName(intentMock.getId().toString());

        Assert.assertNotNull(result);
        Assert.assertEquals(expected, result);
    }
}
