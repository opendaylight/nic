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
import org.opendaylight.nic.compiler.api.Endpoint;
import org.opendaylight.nic.compiler.api.IntentCompiler;
import org.opendaylight.nic.compiler.api.Policy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class IntentCompilerImplTest {
    Set<Policy> policies;
    IntentCompiler intentCompiler;

    private Set<Endpoint> endpoints(String... hosts) throws UnknownHostException {
        Set<Endpoint> endpoints = new LinkedHashSet<>();
        for (String host : hosts) {
            Endpoint endpoint = new EndpointImpl(InetAddress.getByName(host));
            endpoints.add(endpoint);
        }
        return endpoints;
    }

    protected IntentCompiler getIntentCompiler() {
        return new IntentCompilerImpl();
    }

    @Before
    public void setUp() throws Exception {
        policies = new LinkedHashSet<>();
        intentCompiler = getIntentCompiler();
    }

    @Test
    public void testEmptyCompile() throws Exception {
        Collection<Policy> compiledPolicies = intentCompiler.compile(policies);
        assertNotNull(compiledPolicies);
        assertTrue(compiledPolicies.isEmpty());
    }

    @Test
    public void testEmptyEndpointsCompile() throws Exception {
        policies.add(new PolicyImpl(endpoints(), endpoints(), Action.ALLOW));
        Collection<Policy> compiledPolicies = intentCompiler.compile(policies);
        assertNotNull(compiledPolicies);
        assertEquals(policies.size(), compiledPolicies.size());
        assertTrue(compiledPolicies.containsAll(policies));
    }

    @Test
    public void testNonConflictCompile() throws Exception {
        policies.add(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.2"), Action.ALLOW));
        policies.add(new PolicyImpl(endpoints("10.0.0.3"), endpoints("10.0.0.4"), Action.BLOCK));
        Collection<Policy> compiledPolicies = intentCompiler.compile(policies);
        assertNotNull(compiledPolicies);
        assertEquals(policies.size(), compiledPolicies.size());
        assertTrue(compiledPolicies.containsAll(policies));
    }

    @Test
    public void testConflictCompile() throws Exception {
        policies.add(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.2"), Action.ALLOW));
        Policy block = new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.2"), Action.BLOCK);
        policies.add(block);
        Collection<Policy> compiledPolicies = intentCompiler.compile(policies);
        assertNotNull(compiledPolicies);
        assertEquals(1, compiledPolicies.size());
        assertTrue(compiledPolicies.contains(block));
    }

    @Test
    public void testConflictMergeCompile() throws Exception {
        policies.add(new PolicyImpl(endpoints("10.0.0.1", "10.0.0.2"), endpoints("10.0.0.3"), Action.ALLOW));
        Policy block = new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.3"), Action.BLOCK);
        policies.add(block);
        Collection<Policy> compiledPolicies = intentCompiler.compile(policies);
        assertNotNull(compiledPolicies);
        assertEquals(2, compiledPolicies.size());
        assertTrue(compiledPolicies.contains(block));
        assertTrue(compiledPolicies.contains(new PolicyImpl(endpoints("10.0.0.2"), endpoints("10.0.0.3"), Action.ALLOW)));
    }

    @Test
    public void testConflictThreeCompile() throws Exception {
        policies.add(new PolicyImpl(endpoints("10.0.0.1", "10.0.0.2"), endpoints("10.0.0.3"), Action.ALLOW));
        Policy block1 = new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.3"), Action.BLOCK);
        policies.add(block1);
        PolicyImpl block2 = new PolicyImpl(endpoints("10.0.0.2"), endpoints("10.0.0.3"), Action.BLOCK);
        policies.add(block2);
        Collection<Policy> compiledPolicies = intentCompiler.compile(policies);
        assertNotNull(compiledPolicies);
        assertEquals(2, compiledPolicies.size());
        assertTrue(compiledPolicies.contains(block1));
        assertTrue(compiledPolicies.contains(block2));
    }

    @Test
    public void testConflictDestinationCompile() throws Exception {
        policies.add(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.2", "10.0.0.3"), Action.ALLOW));
        Policy block = new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.2"), Action.BLOCK);
        policies.add(block);
        Collection<Policy> compiledPolicies = intentCompiler.compile(policies);
        assertNotNull(compiledPolicies);
        assertEquals(2, compiledPolicies.size());
        assertTrue(compiledPolicies.contains(block));
        assertTrue(compiledPolicies.contains(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.3"), Action.ALLOW)));
    }

    @Test
    public void testConflictDestination2Compile() throws Exception {
        policies.add(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.2", "10.0.0.3"), Action.ALLOW));
        policies.add(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.2", "10.0.0.5"), Action.BLOCK));
        Collection<Policy> compiledPolicies = intentCompiler.compile(policies);
        assertNotNull(compiledPolicies);
        assertEquals(3, compiledPolicies.size());
        assertTrue(compiledPolicies.contains(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.3"), Action.ALLOW)));
        assertTrue(compiledPolicies.contains(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.2"), Action.BLOCK)));
        assertTrue(compiledPolicies.contains(new PolicyImpl(endpoints("10.0.0.1"), endpoints("10.0.0.5"), Action.BLOCK)));
    }
}
