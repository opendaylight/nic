package org.opendaylight.nic.graph.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.nic.graph.api.TermType;

/**
 * Created by yrineu on 25/04/16.
 */
public class TermImplTest {

    private TermType ethTermType;
    private TermType ipProtoTermType;
    private TermType l4DestTermType;
    private TermType l4SrcTermType;
    private TermType vlanTermType;

    private TermImpl ethTermMock;
    private TermImpl ipProtoTermMock;
    private TermImpl l4DestTermMock;
    private TermImpl l4SrcTermMock;
    private TermImpl vlanTermMock;

    @Before
    public void setUp() {
        ethTermType = EthTypeTermType.getInstance();
        ipProtoTermType = IpProtoTermType.getInstance();
        l4DestTermType = L4DstTermType.getInstance();
        l4SrcTermType = L4SrcTermType.getInstance();
        vlanTermType = VlanTermType.getInstance();

        ethTermMock = TermImpl.getInstance(ethTermType);
        ipProtoTermMock = TermImpl.getInstance(ipProtoTermType);
        l4DestTermMock = TermImpl.getInstance(l4DestTermType);
        l4SrcTermMock = TermImpl.getInstance(l4SrcTermType);
        vlanTermMock = TermImpl.getInstance(vlanTermType);
    }

    @Test
    public void testGetInstance() {
        TermType ethType = ethTermMock.getType();

        Assert.assertNotNull(ethTermType);
        Assert.assertNotNull(ethType);
        Assert.assertEquals(ethType, ethTermType);
    }

    @Test
    public void testGreaterThanAndLessThan() {
        final TermImpl ethMax = TermImpl.getInstanceMax(ethTermType);
        final TermImpl ipProtoMax = TermImpl.getInstanceMax(ipProtoTermType);
        final TermImpl l4DestMax = TermImpl.getInstanceMax(l4DestTermType);
        final TermImpl l4SrcMax = TermImpl.getInstanceMax(l4SrcTermType);
        final TermImpl vlanMax = TermImpl.getInstanceMax(vlanTermType);

        boolean greaterThan;
        boolean lessThan;

        greaterThan = ethMax.greaterThan(ethTermMock);
        lessThan = ethMax.lessThan(ethTermMock);
        Assert.assertTrue(greaterThan);
        Assert.assertFalse(lessThan);

        greaterThan = ipProtoMax.greaterThan(ipProtoTermMock);
        lessThan = ipProtoMax.lessThan(ipProtoTermMock);
        Assert.assertTrue(greaterThan);
        Assert.assertFalse(lessThan);

        greaterThan = l4DestMax.greaterThan(l4DestTermMock);
        lessThan = ethMax.lessThan(l4DestTermMock);
        Assert.assertTrue(greaterThan);
        Assert.assertFalse(lessThan);

        greaterThan = l4SrcMax.greaterThan(l4SrcTermMock);
        lessThan = l4SrcMax.lessThan(l4SrcTermMock);
        Assert.assertTrue(greaterThan);
        Assert.assertFalse(lessThan);

        greaterThan = vlanMax.greaterThan(vlanTermMock);
        lessThan = vlanMax.lessThan(vlanTermMock);
        Assert.assertTrue(greaterThan);
        Assert.assertFalse(lessThan);

        greaterThan = ethMax.greaterThan(vlanTermMock);
        lessThan = ethMax.lessThan(vlanTermMock);
        Assert.assertFalse(greaterThan);
        Assert.assertFalse(lessThan);

        Assert.assertTrue(!ethMax.getIntervals().isEmpty());
    }

    @Test
    public void testAddOtherTerm() {
        final TermImpl sameTerm = TermImpl.getInstance(ethTermType);

        TermImpl result = ethTermMock.add(sameTerm);
        Assert.assertEquals(ethTermMock, result);
        Assert.assertEquals(ethTermMock.getIntervals(), result.getIntervals());

        final TermImpl otherTerm = TermImpl.getInstance(vlanTermType);
        result = ethTermMock.add(otherTerm);
        Assert.assertEquals(ethTermMock, result);
    }

    @Test
    public void testAndMethod() {
        final TermImpl otherEthTerm = TermImpl.getInstance(ethTermType);
        TermImpl result;
        result = ethTermMock.and(vlanTermMock);

        Assert.assertEquals(ethTermMock, result);

        result = ethTermMock.and(otherEthTerm);
        Assert.assertEquals(otherEthTerm, result);
        Assert.assertEquals(otherEthTerm.getIntervals(), result.getIntervals());
    }
}