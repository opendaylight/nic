package org.opendaylight.nic.of.renderer.utils;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.nic.of.renderer.utils.MatchUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;

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
        result = MatchUtils.createMplsLabelBosMatch(matchBuilderMock, 2L, true);
        Assert.assertNotNull(result);

        result = MatchUtils.createMplsLabelBosMatch(matchBuilderMock, 2L, false);
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
        MatchUtils.createMplsLabelBosMatch(null, 2L, true);
        MatchUtils.createMplsLabelBosMatch(null, null, true);
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
}