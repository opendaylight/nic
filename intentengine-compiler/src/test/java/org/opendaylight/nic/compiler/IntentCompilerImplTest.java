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
    IntentCompilerImpl intentCompiler;

    private Set<Endpoint> endpoints(String... hosts) throws UnknownHostException {
        Set<Endpoint> endpoints = new LinkedHashSet<>();
        for (String host : hosts) {
            Endpoint endpoint = new EndpointImpl(InetAddress.getByName(host));
            endpoints.add(endpoint);
        }
        return endpoints;
    }

    private void testCompile(Collection<Policy> input, Collection<Policy> output) throws Exception {
        Collection<Policy> compiledPolicies = intentCompiler.compile(input);
        assertNotNull(compiledPolicies);
        assertEquals(output.size(), compiledPolicies.size());
        assertTrue(compiledPolicies.containsAll(output));
    }

    @Before
    public void setUp() throws Exception {
        intentCompiler = new IntentCompilerImpl();
    }

    @Test
    public void testEmptyCompile() throws Exception {
        testCompile(new LinkedList<Policy>(), new LinkedList<Policy>());
    }

    @Test
    public void testEmptyEndpointsCompile() throws Exception {
        testCompile(Arrays.asList(
                intentCompiler.createPolicy(endpoints(), endpoints(), Action.ALLOW)
        ), Arrays.asList(
                intentCompiler.createPolicy(endpoints(), endpoints(), Action.ALLOW)
        ));
    }

    @Test
    public void testNonConflictCompile() throws Exception {
        testCompile(Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"), Action.ALLOW),
                intentCompiler.createPolicy(endpoints("10.0.0.3"), endpoints("10.0.0.4"), Action.BLOCK)
        ), Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"), Action.ALLOW),
                intentCompiler.createPolicy(endpoints("10.0.0.3"), endpoints("10.0.0.4"), Action.BLOCK)
        ));
    }

    @Test
    public void testConflictCompile() throws Exception {
        testCompile(Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"), Action.ALLOW),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"), Action.BLOCK)
        ), Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"), Action.BLOCK)
        ));
    }

    @Test
    public void testConflictMergeCompile() throws Exception {
        testCompile(Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1", "10.0.0.2"), endpoints("10.0.0.3"), Action.ALLOW),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.3"), Action.BLOCK)
        ), Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.2"), endpoints("10.0.0.3"), Action.ALLOW),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.3"), Action.BLOCK)
        ));
    }

    @Test
    public void testConflictThreeCompile() throws Exception {
        testCompile(Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1", "10.0.0.2"), endpoints("10.0.0.3"), Action.ALLOW),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.3"), Action.BLOCK),
                intentCompiler.createPolicy(endpoints("10.0.0.2"), endpoints("10.0.0.3"), Action.BLOCK)
        ), Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.3"), Action.BLOCK),
                intentCompiler.createPolicy(endpoints("10.0.0.2"), endpoints("10.0.0.3"), Action.BLOCK)
        ));
    }

    @Test
    public void testConflictDestinationCompile() throws Exception {
        testCompile(Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2", "10.0.0.3"), Action.ALLOW),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"), Action.BLOCK)
        ), Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.3"), Action.ALLOW),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"), Action.BLOCK)
        ));
    }

    @Test
    public void testConflictDestination2Compile() throws Exception {
        testCompile(Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2", "10.0.0.3"), Action.ALLOW),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2", "10.0.0.5"), Action.BLOCK)
        ), Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.3"), Action.ALLOW),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"), Action.BLOCK),
                intentCompiler.createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.5"), Action.BLOCK)
        ));
    }
}
