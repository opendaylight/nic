/*
 * Copyright © 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;

import static org.junit.Assert.assertNotNull;

public class MatchUtilsTest {

    private MatchBuilder result = null;

    @Mock
    private MatchBuilder matchBuilderMock;

    @Mock
    private NodeConnectorId nodeConnectorId;

    @Mock
    private MacAddress macAddressMock;

    @Mock
    private Ipv4Prefix ipv4PrefixMock;

    private final Long SRC_END_POINT = 5L;
    private final Long IN_PORT = 2L;
    private final Integer VLAN_ID = 100;
    private final BigInteger TUNNEL_ID = BigInteger.TEN;
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void callPrivateConstructorsForCodeCoverage() throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?>[] classesToConstruct = {MatchUtils.class};
        for(Class<?> clazz : classesToConstruct)
        {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            assertNotNull(constructor.newInstance());
        }
    }

    @Test
    public void testCreateInPortMatch() {
        MatchBuilder result = null;

        result = MatchUtils.createInPortMatch(matchBuilderMock, SRC_END_POINT, IN_PORT);
        Assert.assertNotNull(result);

        result = MatchUtils.createInPortMatch(matchBuilderMock, 0L, IN_PORT);
        Assert.assertNotNull(result);

        result = MatchUtils.createInPortMatch(matchBuilderMock, 0L, 0L);
        Assert.assertNotNull(result);

        result = MatchUtils.createInPortMatch(matchBuilderMock, -1L, -2L);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateInPortMatch2() {
        MatchBuilder result = null;

        result = MatchUtils.createInPortMatch(matchBuilderMock, nodeConnectorId);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateArpDstIpv4Match() {
        result = MatchUtils.createArpDstIpv4Match(matchBuilderMock, ipv4PrefixMock);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateSrcL3IPv4Match() {
        result = MatchUtils.createSrcL3IPv4Match(matchBuilderMock, ipv4PrefixMock);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateSetSrcTcpMatch(){
        PortNumber tcpPort = PortNumber.getDefaultInstance("8080");
        result = MatchUtils.createSetSrcTcpMatch(matchBuilderMock, tcpPort);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateSetDstTcpMatch(){
        PortNumber tcpPort = PortNumber.getDefaultInstance("8080");
        result = MatchUtils.createSetDstTcpMatch(matchBuilderMock, tcpPort);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateSetSrcUdpMatch(){
        PortNumber tcpPort = PortNumber.getDefaultInstance("8080");
        result = MatchUtils.createSetSrcUdpMatch(matchBuilderMock, tcpPort);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateSetDstUdpMatch(){
        PortNumber tcpPort = PortNumber.getDefaultInstance("8080");
        result = MatchUtils.createSetDstUdpMatch(matchBuilderMock, tcpPort);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateEthernetTypeMatch() {
        result = MatchUtils.createEtherTypeMatch(matchBuilderMock, 2L);
        Assert.assertNotNull(result);

        result = MatchUtils.createEtherTypeMatch(matchBuilderMock, 0L);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateEthMatch() {
        result = MatchUtils.createEthMatch(matchBuilderMock, macAddressMock, macAddressMock);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateVlanIdMatch() {
        result = MatchUtils.createVlanIdMatch(matchBuilderMock, VLAN_ID, true);
        Assert.assertNotNull(result);

        result = MatchUtils.createVlanIdMatch(matchBuilderMock, VLAN_ID, false);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateMplsLabelBosMatch() {
        result = MatchUtils.createMplsLabelBosMatch(2L, true);
        Assert.assertNotNull(result);

        result = MatchUtils.createMplsLabelBosMatch(2L, false);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateTunnelIDMatch() {
        result = MatchUtils.createTunnelIDMatch(matchBuilderMock, BigInteger.ONE);
        Assert.assertNotNull(result);

        result = MatchUtils.createTunnelIDMatch(matchBuilderMock, BigInteger.TEN);
        Assert.assertNotNull(result);

        result = MatchUtils.createTunnelIDMatch(matchBuilderMock, BigInteger.ZERO);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateICMPv4Match() {
        result = MatchUtils.createICMPv4Match(matchBuilderMock, (short)1, (short)2);
        Assert.assertNotNull(result);

        result = MatchUtils.createICMPv4Match(matchBuilderMock, (short)0, (short)0);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateDstL3IPv4Match() {
        result = MatchUtils.createDstL3IPv4Match(matchBuilderMock, ipv4PrefixMock);
        Assert.assertNotNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testShouldThrowsIllegalArgumentException() {
        MatchUtils.createICMPv4Match(matchBuilderMock, (short)-1, (short)-2);
    }

    @Test(expected = NullPointerException.class)
    public void testThrowNullPointerException() {

        MatchUtils.createInPortMatch(null, SRC_END_POINT, IN_PORT);
        MatchUtils.createInPortMatch(matchBuilderMock, null);
        MatchUtils.createInPortMatch(null, nodeConnectorId);
        MatchUtils.createEtherTypeMatch(null, 2L);
        MatchUtils.createVlanIdMatch(null, VLAN_ID, true);
        MatchUtils.createVlanIdMatch(null, null, true);
        MatchUtils.createMplsLabelBosMatch(2L, true);
        MatchUtils.createMplsLabelBosMatch(null, true);
        MatchUtils.createEthMatch(null, macAddressMock, macAddressMock);
        MatchUtils.createEthMatch(matchBuilderMock, null, macAddressMock);
        MatchUtils.createEthMatch(matchBuilderMock, macAddressMock, null);
        MatchUtils.createEthMatch(null, null, null);
        MatchUtils.createTunnelIDMatch(matchBuilderMock, null);
        MatchUtils.createTunnelIDMatch(null, TUNNEL_ID);
        MatchUtils.createICMPv4Match(null, (short)1, (short) 2);
        MatchUtils.createDstL3IPv4Match(matchBuilderMock, null);
        MatchUtils.createDstL3IPv4Match(null, ipv4PrefixMock);
    }

    @Test
    public void testNullCreateIPv4Match() {
        MatchBuilder matchBuilder = null;
        Ipv4Prefix src = null;
        Ipv4Prefix dst = null;

        MatchUtils.createIPv4PrefixMatch(src, dst, matchBuilder);

        Assert.assertNull(src);
        Assert.assertNull(dst);
        Assert.assertNull(matchBuilder);
    }

    @Test
    public void testCreateIPv4Match() {
        long IPV4_LONG = 0x800;
        EtherType ethType = new EtherType(IPV4_LONG);

        MatchBuilder matchBuilder = new MatchBuilder();
        Ipv4Prefix src = new Ipv4Prefix("10.0.0.1/8");
        Ipv4Prefix dst = new Ipv4Prefix("10.0.0.2/8");

        MatchUtils.createIPv4PrefixMatch(src, dst, matchBuilder);
        Ipv4Match ipv4Match = (Ipv4Match) matchBuilder.getLayer3Match();

        Assert.assertEquals(ethType.getValue(), matchBuilder.getEthernetMatch().getEthernetType().getType().getValue());
        Assert.assertNotNull(ipv4Match.getIpv4Source());
        Assert.assertNotNull(ipv4Match.getIpv4Destination());
    }

    @Test
    public void testCreateIPv4MatchSource() {
        MatchBuilder matchBuilder = new MatchBuilder();
        Ipv4Prefix src = new Ipv4Prefix("10.0.0.1/8");
        Ipv4Prefix dst = null;

        MatchUtils.createIPv4PrefixMatch(src, dst, matchBuilder);
        Ipv4Match ipv4Match = (Ipv4Match) matchBuilder.getLayer3Match();

        Assert.assertNotNull(ipv4Match.getIpv4Source());
        Assert.assertNull(ipv4Match.getIpv4Destination());
    }

    @Test
    public void testCreateIPv4MatchDestination() {
        MatchBuilder matchBuilder = new MatchBuilder();
        Ipv4Prefix src = null;
        Ipv4Prefix dst = new Ipv4Prefix("10.0.0.1/8");

        MatchUtils.createIPv4PrefixMatch(src, dst, matchBuilder);
        Ipv4Match ipv4Match = (Ipv4Match) matchBuilder.getLayer3Match();

        Assert.assertNull(ipv4Match.getIpv4Source());
        Assert.assertNotNull(ipv4Match.getIpv4Destination());
    }

    @Test
    public void testNullCreateIPv6Match() {
        MatchBuilder matchBuilder = null;
        Ipv4Prefix src = null;
        Ipv4Prefix dst = null;

        MatchUtils.createIPv4PrefixMatch(src, dst, matchBuilder);

        Assert.assertNull(src);
        Assert.assertNull(dst);
        Assert.assertNull(matchBuilder);
    }

    @Test
    public void testCreateIPv6Match() {
        long IPV6_LONG = 0x86DD;
        EtherType ethType = new EtherType(IPV6_LONG);

        MatchBuilder matchBuilder = new MatchBuilder();
        Ipv6Prefix src = new Ipv6Prefix("2001:db8:a0b:12f0::1/24");
        Ipv6Prefix dst = new Ipv6Prefix("2001:db8:a0b:12f0::1/8");

        MatchUtils.createIPv6PrefixMatch(src, dst, matchBuilder);
        Ipv6Match ipv6Match = (Ipv6Match) matchBuilder.getLayer3Match();

        Assert.assertEquals(ethType.getValue(), matchBuilder.getEthernetMatch().getEthernetType().getType().getValue());
        Assert.assertNotNull(ipv6Match.getIpv6Source());
        Assert.assertNotNull(ipv6Match.getIpv6Destination());
    }

    @Test
    public void testCreateIPv6MatchSource() {
        MatchBuilder matchBuilder = new MatchBuilder();
        Ipv6Prefix src = new Ipv6Prefix("2001:db8:a0b:12f0::1/24");
        Ipv6Prefix dst = null;

        MatchUtils.createIPv6PrefixMatch(src, dst, matchBuilder);
        Ipv6Match ipv6Match = (Ipv6Match) matchBuilder.getLayer3Match();

        Assert.assertNotNull(ipv6Match.getIpv6Source());
        Assert.assertNull(ipv6Match.getIpv6Destination());
    }

    @Test
    public void testCreateIPv6MatchDestination() {
        MatchBuilder matchBuilder = new MatchBuilder();
        Ipv6Prefix src = null;
        Ipv6Prefix dst = new Ipv6Prefix("2001:db8:a0b:12f0::1/24");

        MatchUtils.createIPv6PrefixMatch(src, dst, matchBuilder);
        Ipv6Match ipv6Match = (Ipv6Match) matchBuilder.getLayer3Match();

        Assert.assertNull(ipv6Match.getIpv6Source());
        Assert.assertNotNull(ipv6Match.getIpv6Destination());
    }
}