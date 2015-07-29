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
import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.nic.compiler.api.Action;
import org.opendaylight.nic.compiler.api.ActionConflictType;
import org.opendaylight.nic.compiler.api.BasicAction;
import org.opendaylight.nic.compiler.api.Endpoint;
import org.opendaylight.nic.compiler.api.IntentCompilerException;
import org.opendaylight.nic.compiler.api.Policy;

/**
 * JUnit test for {@link TransformTest}.
 */
public class TransformTest {
    private Transform transform;
    private Action allow, block, redirect, allowExculsive;
    private Policy policyOne, policyTwo, policyThree;
    private static final String[] SOURCE_IPS = {"10.0.0.1", "10.0.0.2"};
    private static final String[] DESTINATION_IPS = {"10.0.0.3", "10.0.0.4"};
    private static final String ACTION_ALLOW = "ALLOW";
    private static final String ACTION_BLOCK = "BLOCK";
    private static final String ACTION_REDIRECT = "BLOCK";
    private Collection<Policy> policies;

    /**
     * This method creates the required objects to perform unit testing.
     */
    @Before
    public void setUp() throws Exception {
        policies = new LinkedList<>();
        transform = new Transform();
        allow = new BasicAction(ACTION_ALLOW, ActionConflictType.COMPOSABLE);
        allowExculsive = new BasicAction(ACTION_ALLOW, ActionConflictType.EXCLUSIVE);
        block = new BasicAction(ACTION_BLOCK, ActionConflictType.EXCLUSIVE);
        redirect = new BasicAction(ACTION_REDIRECT, ActionConflictType.OBSERVER);
    }

    private Set<Endpoint> endpoints(String... hosts) throws UnknownHostException {
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

    /**
     * Test to verify if resolve() method work
     * for the empty Policy object.
     */
    @Test
    public void testEmptyResolve() throws Exception {
        Set<Endpoint> srcAddress = new LinkedHashSet<>();
        Set<Endpoint> dstAddress = new LinkedHashSet<>();
        Set<Action> actions = new LinkedHashSet<>();
        policyOne = new PolicyImpl(srcAddress,dstAddress,actions);
        policyTwo = new PolicyImpl(srcAddress,dstAddress,actions);
        assertEquals(0, transform.resolve(policyOne, policyTwo).size());
    }

    /**
     * Test to verify if resolve() method work for the Policy objects
     * with passing different source,different destination and different action with differ.
     *
     */
    @Test
    public void testResolve() throws Exception {
        policyOne = new PolicyImpl(endpoints(SOURCE_IPS[0]), endpoints(DESTINATION_IPS[0]), actions(block));
        policyTwo = new PolicyImpl(endpoints(SOURCE_IPS[1]), endpoints(DESTINATION_IPS[1]), actions(allow));
        policies.add(policyOne);
        policies.add(policyTwo);
        assertNotNull(transform.resolve(policyOne, policyTwo));
        assertEquals(2, transform.resolve(policyOne, policyTwo).size());
        assertEquals(policies, transform.resolve(policyOne, policyTwo));
    }

    /**
     * Test to verify if resolve() method work for the Policy objects
     * with passing different source,same destination and same action.
     */
    @Test
    public void testDstResolve() throws Exception {
        policyOne = new PolicyImpl(endpoints(SOURCE_IPS[0]), endpoints(DESTINATION_IPS[1]), actions(block));
        policyTwo = new PolicyImpl(endpoints(SOURCE_IPS[1]), endpoints(DESTINATION_IPS[1]), actions(allow));
        policies.add(policyOne);
        policies.add(policyTwo);
        assertNotNull(transform.resolve(policyOne, policyTwo));
        assertEquals(2, transform.resolve(policyOne, policyTwo).size());
        assertEquals(policies, transform.resolve(policyOne, policyTwo));
    }

    /**
     * Test to verify if resolve() method work for the Policy objects
     * with passing same source,same destination and different action.
     */
    @Test
    public void testSourceResolve() throws Exception {
        policyOne = new PolicyImpl(endpoints(SOURCE_IPS[0]), endpoints(DESTINATION_IPS[1]), actions(block));
        policyTwo = new PolicyImpl(endpoints(SOURCE_IPS[0]), endpoints(DESTINATION_IPS[1]), actions(redirect));
        policyThree = new PolicyImpl(endpoints(SOURCE_IPS[0]), endpoints(DESTINATION_IPS[1]), actions(block,redirect));
        policies.add(policyThree);
        assertNotNull(transform.resolve(policyOne, policyTwo));
        assertEquals(1, transform.resolve(policyOne, policyTwo).size());
        assertEquals(policies, transform.resolve(policyOne, policyTwo));
    }

    /**
     * Test to verify if resolve() method work for the Policy objects
     * with passing same source,same destination and different action.
     */
    @Test
    public void testActionResolve() throws Exception {
        policyOne = new PolicyImpl(endpoints(SOURCE_IPS[0]), endpoints(DESTINATION_IPS[1]), actions(redirect));
        policyTwo = new PolicyImpl(endpoints(SOURCE_IPS[0]), endpoints(DESTINATION_IPS[1]), actions(allow));
        policyThree = new PolicyImpl(endpoints(SOURCE_IPS[0]), endpoints(DESTINATION_IPS[1]), actions(allow, redirect));
        policies.add(policyThree);
        assertNotNull(transform.resolve(policyOne, policyTwo));
        assertEquals(1, transform.resolve(policyOne, policyTwo).size());
        assertEquals(policies, transform.resolve(policyOne, policyTwo));
    }

    /**
     * Test to verify if resolve() method work for the Policy objects
     * with passing same source,same destination and different action.
     */
    @Test
    public void testActionEmptyResolve() throws Exception {
        policyOne = new PolicyImpl(endpoints(SOURCE_IPS[0]), endpoints(DESTINATION_IPS[1]), actions(allow));
        policyTwo = new PolicyImpl(endpoints(SOURCE_IPS[0]), endpoints(DESTINATION_IPS[1]), actions(block));
        policyThree = new PolicyImpl(endpoints(SOURCE_IPS[0]), endpoints(DESTINATION_IPS[1]), actions(block));
        policies.add(policyThree);
        assertNotNull(transform.resolve(policyOne, policyTwo));
        assertEquals(1, transform.resolve(policyOne, policyTwo).size());
        assertEquals(policies, transform.resolve(policyOne, policyTwo));
    }

    /**
     * Test to verify if resolve() method work for the Policy objects
     * with passing same source,same destination and multiple action with different action type.
     */
    @Test
    public void testSingleActionResolve() throws Exception {
        policyOne = new PolicyImpl(endpoints(SOURCE_IPS[0]), endpoints(DESTINATION_IPS[1]), actions(allowExculsive, allow));
        policyTwo = new PolicyImpl(endpoints(SOURCE_IPS[0]), endpoints(DESTINATION_IPS[1]), actions(allowExculsive, block));
        try {
            transform.resolve(policyOne, policyTwo);
        }
        catch(IntentCompilerException compiler) {
            assertEquals("Unable to merge exclusive actions", compiler.getMessage());
        }
    }

    /**
     * Test to verify if resolve() method work for the Policy objects
     * with passing same source,different destination and same action.
     */
    @Test
    public void testSameSourceResolve() throws Exception {
        Set<Action> actionConflict = new LinkedHashSet<>();
        Action action = new BasicAction("");
        actionConflict.add(action);
        policyOne = new PolicyImpl(endpoints(SOURCE_IPS[0]), endpoints(DESTINATION_IPS[0]), actions(allow));
        policyTwo = new PolicyImpl(endpoints(SOURCE_IPS[0]), endpoints(DESTINATION_IPS[1]), actions(allow));
        transform.resolve(policyOne, policyTwo);
        policies.add(policyOne);
        policies.add(policyTwo);
        assertNotNull(transform.resolve(policyOne, policyTwo));
        assertEquals(2, transform.resolve(policyOne, policyTwo).size());
        assertEquals(policies, transform.resolve(policyOne, policyTwo));
    }
}
