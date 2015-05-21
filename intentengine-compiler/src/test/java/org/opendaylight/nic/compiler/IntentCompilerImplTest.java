//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.nic.compiler.api.Action;
import org.opendaylight.nic.compiler.api.ActionType;
import org.opendaylight.nic.compiler.api.Endpoint;
import org.opendaylight.nic.compiler.api.Policy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class IntentCompilerImplTest {
    Set<Policy> policies;
    IntentCompilerImpl intentCompiler;
    Action allow, block, redirect, monitor;

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

    class Allow implements Action {
        @Override
        public String getName() {
            return "ALLOW";
        }

        @Override
        public ActionType getType() {
            return ActionType.COMPOSABLE;
        }
    }

    class Block implements Action {
        @Override
        public String getName() {
            return "BLOCK";
        }

        @Override
        public ActionType getType() {
            return ActionType.EXCLUSIVE;
        }
    }

    class Redirect implements Action {
        @Override
        public String getName() {
            return "REDIRECT";
        }

        @Override
        public ActionType getType() {
            return ActionType.COMPOSABLE;
        }
    }

    class Monitor implements Action {
        @Override
        public String getName() {
            return "MONITOR";
        }

        @Override
        public ActionType getType() {
            return ActionType.OBSERVER;
        }
    }

    @Before
    public void setUp() throws Exception {
        policies = new LinkedHashSet<>();
        intentCompiler = new IntentCompilerImpl();
        allow = new Allow();
        block = new Block();
        redirect = new Redirect();
        monitor = new Monitor();
    }

    @Test
    public void testEmptyCompile() throws Exception {
        Collection<Policy> compiledPolicies = intentCompiler.compile(policies);
        assertNotNull(compiledPolicies);
        assertTrue(compiledPolicies.isEmpty());
    }

    @Test
    public void testEmptyEndpointsCompile() throws Exception {
        policies.add(new PolicyImpl(endpoints(), endpoints(), actions(allow)));
        Collection<Policy> compiledPolicies = intentCompiler.compile(policies);
        assertNotNull(compiledPolicies);
        assertEquals(policies.size(), compiledPolicies.size());
        assertTrue(compiledPolicies.containsAll(policies));
    }

    @Test
    public void testNonConflictCompile() throws Exception {
        policies.add(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(allow)));
        policies.add(new PolicyImpl(endpoints("10.0.0.3"), endpoints("10.0.0.4"), actions(block)));
        Collection<Policy> compiledPolicies = intentCompiler.compile(policies);
        assertNotNull(compiledPolicies);
        assertEquals(policies.size(), compiledPolicies.size());
        assertTrue(compiledPolicies.containsAll(policies));
    }

    @Test
    public void testConflictCompile() throws Exception {
        policies.add(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(allow)));
        Policy block1 = new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(block));
        policies.add(block1);
        Collection<Policy> compiledPolicies = intentCompiler.compile(policies);
        assertNotNull(compiledPolicies);
        assertEquals(1, compiledPolicies.size());
        assertTrue(compiledPolicies.contains(block1));
    }

    @Test
    public void testConflictMergeCompile() throws Exception {
        policies.add(new PolicyImpl(endpoints("10.0.0.1", "10.0.0.2"), endpoints("10.0.0.3"), actions(allow)));
        Policy block1 = new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.3"), actions(block));
        policies.add(block1);
        Collection<Policy> compiledPolicies = intentCompiler.compile(policies);
        assertNotNull(compiledPolicies);
        assertEquals(2, compiledPolicies.size());
        assertTrue(compiledPolicies.contains(block1));
        assertTrue(compiledPolicies.contains(new PolicyImpl(endpoints("10.0.0.2"), endpoints("10.0.0.3"), actions(allow))));
    }

    @Test
    public void testConflictThreeCompile() throws Exception {
        policies.add(new PolicyImpl(endpoints("10.0.0.1", "10.0.0.2"), endpoints("10.0.0.3"), actions(allow)));
        Policy block1 = new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.3"), actions(block));
        policies.add(block1);
        PolicyImpl block2 = new PolicyImpl(endpoints("10.0.0.2"), endpoints("10.0.0.3"), actions(block));
        policies.add(block2);
        Collection<Policy> compiledPolicies = intentCompiler.compile(policies);
        assertNotNull(compiledPolicies);
        assertEquals(2, compiledPolicies.size());
        assertTrue(compiledPolicies.contains(block1));
        assertTrue(compiledPolicies.contains(block2));
    }

    @Test
    public void testConflictDestinationCompile() throws Exception {
        policies.add(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.2", "10.0.0.3"), actions(allow)));
        Policy block1 = new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(block));
        policies.add(block1);
        Collection<Policy> compiledPolicies = intentCompiler.compile(policies);
        assertNotNull(compiledPolicies);
        assertEquals(2, compiledPolicies.size());
        assertTrue(compiledPolicies.contains(block1));
        assertTrue(compiledPolicies.contains(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.3"), actions(allow))));
    }

    @Test
    public void testConflictDestination2Compile() throws Exception {
        policies.add(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.2", "10.0.0.3"), actions(allow)));
        policies.add(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.2", "10.0.0.5"), actions(block)));
        Collection<Policy> compiledPolicies = intentCompiler.compile(policies);
        assertNotNull(compiledPolicies);
        assertEquals(3, compiledPolicies.size());
        assertTrue(compiledPolicies.contains(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.3"), actions(allow))));
        assertTrue(compiledPolicies.contains(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(block))));
        assertTrue(compiledPolicies.contains(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.5"), actions(block))));
    }

    @Test
    public void testConflictMergeActionsCompile() throws Exception {
        policies.add(new PolicyImpl(endpoints("10.0.0.1", "10.0.0.2", "10.0.0.10"), endpoints("10.0.0.3"), actions(allow)));
        policies.add(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.3", "10.0.0.20"), actions(block)));
        policies.add(new PolicyImpl(endpoints("10.0.0.2"), endpoints("10.0.0.3"), actions(redirect)));
        policies.add(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.20"), actions(monitor)));
        Collection<Policy> compiledPolicies = intentCompiler.compile(policies);
        assertNotNull(compiledPolicies);
        assertEquals(4, compiledPolicies.size());
        assertTrue(compiledPolicies.contains(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.3"), actions(block))));
        assertTrue(compiledPolicies.contains(new PolicyImpl(endpoints("10.0.0.10"), endpoints("10.0.0.3"), actions(allow))));
        assertTrue(compiledPolicies.contains(new PolicyImpl(endpoints("10.0.0.2"), endpoints("10.0.0.3"), actions(allow, redirect))));
        assertTrue(compiledPolicies.contains(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.20"), actions(block, monitor))));
    }
}
