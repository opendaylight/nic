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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
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

    private void testCompile(Collection<Policy> input, Collection<Policy> output) throws Exception {
        Collection<Policy> compiledPolicies = intentCompiler.compile(input);
        assertNotNull(compiledPolicies);
        assertEquals(output.size(), compiledPolicies.size());
        assertTrue(compiledPolicies.containsAll(output));
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
        testCompile(policies, new LinkedList<Policy>());
    }

    @Test
    public void testEmptyEndpointsCompile() throws Exception {
        testCompile(Arrays.asList(
                intentCompiler.createPolicy(endpoints(), endpoints(), actions(allow))
        ), Arrays.asList(
                intentCompiler.createPolicy(endpoints(), endpoints(), actions(allow))
        ));
    }

    @Test
    public void testNonConflictCompile() throws Exception {
        testCompile(Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(allow)),
                intentCompiler.createPolicy(endpoints("10.0.0.3"), endpoints("10.0.0.4"), actions(block))
        ), Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(allow)),
                intentCompiler.createPolicy(endpoints("10.0.0.3"), endpoints("10.0.0.4"), actions(block))
        ));
    }

    @Test
    public void testConflictCompile() throws Exception {
        testCompile(Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(allow)),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(block))
        ), Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(block))
        ));
    }

    @Test
    public void testConflictMergeCompile() throws Exception {
        testCompile(Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1", "10.0.0.2"), endpoints("10.0.0.3"), actions(allow)),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.3"), actions(block))
        ), Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.2"), endpoints("10.0.0.3"), actions(allow)),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.3"), actions(block))
        ));
    }

    @Test
    public void testConflictThreeCompile() throws Exception {
        testCompile(Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1", "10.0.0.2"), endpoints("10.0.0.3"), actions(allow)),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.3"), actions(block)),
                intentCompiler.createPolicy(endpoints("10.0.0.2"), endpoints("10.0.0.3"), actions(block))
        ), Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.3"), actions(block)),
                intentCompiler.createPolicy(endpoints("10.0.0.2"), endpoints("10.0.0.3"), actions(block))
        ));
    }

    @Test
    public void testConflictDestinationCompile() throws Exception {
        testCompile(Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2", "10.0.0.3"), actions(allow)),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(block))
        ), Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.3"), actions(allow)),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(block))
        ));
    }

    @Test
    public void testConflictDestination2Compile() throws Exception {
        testCompile(Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2", "10.0.0.3"), actions(allow)),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2", "10.0.0.5"), actions(block))
        ), Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.3"), actions(allow)),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(block)),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.5"), actions(block))
        ));
    }

    @Test
    public void testConflictMergeActionsCompile() throws Exception {
        testCompile(Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1", "10.0.0.2", "10.0.0.10"), endpoints("10.0.0.3"), actions(allow)),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.3", "10.0.0.20"), actions(block)),
                intentCompiler.createPolicy(endpoints("10.0.0.2"), endpoints("10.0.0.3"), actions(redirect)),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.20"), actions(monitor))
        ), Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.3"), actions(block)),
                intentCompiler.createPolicy(endpoints("10.0.0.10"), endpoints("10.0.0.3"), actions(allow)),
                intentCompiler.createPolicy(endpoints("10.0.0.2"), endpoints("10.0.0.3"), actions(allow, redirect)),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.20"), actions(block, monitor))
        ));
    }
}
