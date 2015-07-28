/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.compiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import org.opendaylight.nic.compiler.api.Action;
import org.opendaylight.nic.compiler.api.ActionConflictType;
import org.opendaylight.nic.compiler.api.BasicAction;
import org.opendaylight.nic.compiler.api.Endpoint;

public class PolicyImplTest {

    private Action allow, block, redirect;
    private PolicyImpl policyImpl, policyTest, policyTestOne, policyTestTwo,
            policyTestThree, policyTestFour, policyTestEmptySrc,
            policyTestEmptyDst, policyTestEmptyAction, policyTestNullSrc, policyTestNullDst, policyTestNullAction;
    private String[] srcIpAddresses = { "10.0.0.1", "10.0.0.3" };
    private String[] dstIpAddresses = { "192.168.196.1", "192.168.196.2" };

    @Before
    public void setUp() throws Exception {

        allow = new BasicAction("ALLOW", ActionConflictType.COMPOSABLE);
        block = new BasicAction("BLOCK", ActionConflictType.EXCLUSIVE);
        redirect = new BasicAction("REDIRECT", ActionConflictType.COMPOSABLE);
        policyImpl = new PolicyImpl(endpoints(srcIpAddresses),
                endpoints(dstIpAddresses), actions(block, allow));
    }

    private Set<Endpoint> endpoints(String... hosts)
            throws UnknownHostException {

        Set<Endpoint> endpoints = new LinkedHashSet<>();
        for (String host : hosts) {
            Endpoint endpoint = new EndpointImpl(InetAddress.getByName(host));
            endpoints.add(endpoint);
        }
        return endpoints;
    }

    private Set<Action> actions(Action... actions) {

        Set<Action> actionSet = new LinkedHashSet<>();
        for (Action action : actions) {
            actionSet.add(action);
        }
        return actionSet;
    }

    @Test
    public void testSrc() throws Exception{

        Set<Endpoint> actualEndpoints = policyImpl.src();
        assertEquals("must be equal", endpoints(srcIpAddresses), actualEndpoints);

    }

    @Test
    public void testDst() throws Exception{

        Set<Endpoint> actualEndpoints = policyImpl.dst();
        assertEquals("must be equal", endpoints(dstIpAddresses), actualEndpoints);

    }

    @Test
    public void testAction() throws Exception{

        Set<Action> actualActions = policyImpl.action();
        assertEquals("must be equal", actions(allow, block), actualActions);

    }

    /**
     * Test to verify if equals() method work
     * for the PolicyImpl object.
     * @throws Exception
     */
    @Test
    public void testEquals() throws Exception {

        policyTestOne = new PolicyImpl(endpoints(srcIpAddresses[0]),
                endpoints(dstIpAddresses[1]), actions(block));
        policyTestNullSrc = new PolicyImpl(null,
                endpoints(dstIpAddresses[1]), actions(block));
        policyTestTwo = new PolicyImpl(endpoints(srcIpAddresses),
                endpoints(dstIpAddresses[0]), actions(block));
        policyTestNullDst = new PolicyImpl(endpoints(srcIpAddresses),
                null, actions(block));
        policyTestThree = new PolicyImpl(endpoints(srcIpAddresses),
                endpoints(dstIpAddresses), actions(redirect));
        policyTestNullAction = new PolicyImpl(endpoints(srcIpAddresses),
                endpoints(dstIpAddresses), null);
        policyTestFour = new PolicyImpl(endpoints(srcIpAddresses),
                endpoints(dstIpAddresses), actions(block, allow));
        policyTestEmptySrc = new PolicyImpl(null, endpoints(dstIpAddresses[1]),
                actions(block));
        policyTestEmptyDst = new PolicyImpl(endpoints(srcIpAddresses), null,
                null);
        policyTestEmptyAction = new PolicyImpl(endpoints(srcIpAddresses),
                endpoints(dstIpAddresses), null);

        assertTrue(policyImpl.equals(policyImpl));
        assertFalse(policyImpl.equals(null));
        assertFalse(policyImpl.equals(new Object()));
        assertEquals(true, policyImpl.equals(policyImpl));
        assertEquals(false, policyImpl.equals(null));
        assertEquals(false, policyImpl.equals(new Object()));
        assertEquals(false, policyImpl.equals(policyTestOne));
        assertEquals(false, policyImpl.equals(policyTestTwo));
        assertEquals(false, policyImpl.equals(policyTestThree));
        assertEquals(true, policyImpl.equals(policyTestFour));
        assertEquals(false, policyTestEmptySrc.equals(policyTestOne));
        assertEquals(true, policyTestEmptySrc.equals(policyTestNullSrc));
        assertEquals(false, policyTestEmptyDst.equals(policyTestTwo));
        assertEquals(false, policyTestEmptyDst.equals(policyTestNullDst));
        assertEquals(false, policyTestEmptyAction.equals(policyTestThree));
        assertEquals(true, policyTestEmptyAction.equals(policyTestNullAction));
    }

    /**
     * Test to verify if toString()  method work
     * for the PolicyImpl object.
     *
     */
    @Test
    public void testToString() {
        final String expectedString = "from [10.0.0.1, 10.0.0.3] to [192.168.196.1, 192.168.196.2] apply [BLOCK, ALLOW]";
        String actualString = policyImpl.toString();
        assertEquals("must be equal", expectedString, actualString);
    }

    /**
     * Test to verify if hashCode() method work
     * for the PolicyImpl object.
     *
     */
    @Test
    public void testHashCode() {
        policyTest = new PolicyImpl(null, null, null);
        assertEquals("must be equal", -1000538377, policyImpl.hashCode());
        assertEquals("must be equal", 0, policyTest.hashCode());
    }
}
