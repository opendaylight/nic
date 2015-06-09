//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.nic.compiler.api.Action;
import org.opendaylight.nic.compiler.api.ActionConflictType;
import org.opendaylight.nic.compiler.api.BasicAction;
import org.opendaylight.nic.compiler.api.Endpoint;
import org.opendaylight.nic.compiler.api.Policy;

public class IntentCompilerImplTest {
    IntentCompilerImpl intentCompiler;
    Action allow;
    Action block;
    Action redirect;
    Action monitor;

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

    private void testCompile(Collection<Policy> input, Collection<Policy> output)
            throws Exception {
        Collection<Policy> compiledPolicies = intentCompiler.compile(input);
        assertNotNull(compiledPolicies);
        assertEquals(output.size(), compiledPolicies.size());
        assertTrue(compiledPolicies.containsAll(output));
    }

    @Before
    public void setUp() throws Exception {
        intentCompiler = new IntentCompilerImpl();
        allow = new BasicAction("ALLOW");
        block = new BasicAction("BLOCK", ActionConflictType.EXCLUSIVE);
        redirect = new BasicAction("REDIRECT", ActionConflictType.COMPOSABLE);
        monitor = new BasicAction("MONITOR", ActionConflictType.OBSERVER);
    }

    @Test
    public void testEmptyCompile() throws Exception {
        testCompile(Collections.<Policy>emptyList(),
                Collections.<Policy>emptyList());
    }

    @Test
    public void testEmptyEndpointsCompile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createPolicy(endpoints(),
                endpoints(), actions(allow))), Arrays.asList(intentCompiler
                .createPolicy(endpoints(), endpoints(), actions(allow))));
    }

    @Test
    public void testNonConflictCompile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createPolicy(
                endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(allow)),
                intentCompiler.createPolicy(endpoints("10.0.0.3"),
                        endpoints("10.0.0.4"), actions(block))), Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"),
                        endpoints("10.0.0.2"), actions(allow)), intentCompiler
                        .createPolicy(endpoints("10.0.0.3"),
                                endpoints("10.0.0.4"), actions(block))));

    }

    @Test
    public void testConflictCompile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createPolicy(
                endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(allow)),
                intentCompiler.createPolicy(endpoints("10.0.0.1"),
                        endpoints("10.0.0.2"), actions(block))),
                Arrays.asList(intentCompiler.createPolicy(
                        endpoints("10.0.0.1"), endpoints("10.0.0.2"),
                        actions(block))));
    }

    @Test
    public void testConflictMergeCompile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createPolicy(
                endpoints("10.0.0.1", "10.0.0.2"), endpoints("10.0.0.3"),
                actions(allow)), intentCompiler.createPolicy(
                endpoints("10.0.0.1"), endpoints("10.0.0.3"), actions(block))),
                Arrays.asList(intentCompiler.createPolicy(
                        endpoints("10.0.0.2"), endpoints("10.0.0.3"),
                        actions(allow)), intentCompiler.createPolicy(
                        endpoints("10.0.0.1"), endpoints("10.0.0.3"),
                        actions(block))));
    }

    @Test
    public void testConflictThreeCompile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createPolicy(
                endpoints("10.0.0.1", "10.0.0.2"), endpoints("10.0.0.3"),
                actions(allow)), intentCompiler.createPolicy(
                endpoints("10.0.0.1"), endpoints("10.0.0.3"), actions(block)),
                intentCompiler.createPolicy(endpoints("10.0.0.2"),
                        endpoints("10.0.0.3"), actions(block))), Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"),
                        endpoints("10.0.0.3"), actions(block)), intentCompiler
                        .createPolicy(endpoints("10.0.0.2"),
                                endpoints("10.0.0.3"), actions(block))));
    }

    @Test
    public void testConflictDestinationCompile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createPolicy(
                endpoints("10.0.0.1"), endpoints("10.0.0.2", "10.0.0.3"),
                actions(allow)), intentCompiler.createPolicy(
                endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(block))),
                Arrays.asList(intentCompiler.createPolicy(
                        endpoints("10.0.0.1"), endpoints("10.0.0.3"),
                        actions(allow)), intentCompiler.createPolicy(
                        endpoints("10.0.0.1"), endpoints("10.0.0.2"),
                        actions(block))));
    }

    @Test
    public void testConflictDestination2Compile() throws Exception {
        testCompile(Arrays.asList(
                intentCompiler.createPolicy(endpoints("10.0.0.1"),
                        endpoints("10.0.0.2", "10.0.0.3"), actions(allow)),
                intentCompiler.createPolicy(endpoints("10.0.0.1"),
                        endpoints("10.0.0.2", "10.0.0.5"), actions(block))),
                Arrays.asList(intentCompiler.createPolicy(
                        endpoints("10.0.0.1"), endpoints("10.0.0.3"),
                        actions(allow)), intentCompiler.createPolicy(
                        endpoints("10.0.0.1"), endpoints("10.0.0.2"),
                        actions(block)), intentCompiler.createPolicy(
                        endpoints("10.0.0.1"), endpoints("10.0.0.5"),
                        actions(block))));
    }

    @Test
    public void testConflictMergeActionsCompile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createPolicy(
                endpoints("10.0.0.1", "10.0.0.2", "10.0.0.10"),
                endpoints("10.0.0.3"), actions(allow)), intentCompiler
                .createPolicy(endpoints("10.0.0.1"),
                        endpoints("10.0.0.3", "10.0.0.20"), actions(block)),
                intentCompiler.createPolicy(endpoints("10.0.0.2"),
                        endpoints("10.0.0.3"), actions(redirect)),
                intentCompiler.createPolicy(endpoints("10.0.0.1"),
                        endpoints("10.0.0.20"), actions(monitor))),
                Arrays.asList(intentCompiler.createPolicy(
                        endpoints("10.0.0.1"), endpoints("10.0.0.3"),
                        actions(block)), intentCompiler.createPolicy(
                        endpoints("10.0.0.10"), endpoints("10.0.0.3"),
                        actions(allow)), intentCompiler.createPolicy(
                        endpoints("10.0.0.2"), endpoints("10.0.0.3"),
                        actions(allow, redirect)), intentCompiler.createPolicy(
                        endpoints("10.0.0.1"), endpoints("10.0.0.20"),
                        actions(block, monitor))));
    }

    @Test
    public void testClassifierNonConflicting() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createPolicy(
                endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(allow),
                ClassifierHelper.vlan(10, 20)), intentCompiler.createPolicy(
                endpoints("10.0.0.3"), endpoints("10.0.0.4"), actions(block),
                ClassifierHelper.vlan(10, 40))), Arrays.asList(intentCompiler
                .createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"),
                        actions(allow), ClassifierHelper.vlan(10, 20)),
                intentCompiler.createPolicy(endpoints("10.0.0.3"),
                        endpoints("10.0.0.4"), actions(block),
                        ClassifierHelper.vlan(10, 40))));

    }

    @Test
    public void testClassifierConflictCompile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createPolicy(
                endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(allow),
                ClassifierHelper.vlan(10, 20)), intentCompiler.createPolicy(
                endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(block),
                ClassifierHelper.vlan(30, 40))), Arrays.asList(intentCompiler
                .createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.2"),
                        actions(allow), ClassifierHelper.vlan(10, 20)),
                intentCompiler.createPolicy(endpoints("10.0.0.1"),
                        endpoints("10.0.0.2"), actions(block),
                        ClassifierHelper.vlan(30, 40))));
    }

    @Test
    public void testClassifier2Compile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createPolicy(
                endpoints("10.0.0.1", "10.0.0.2"), endpoints("10.0.0.3"),
                actions(allow), ClassifierHelper.vlan(5, 20)), intentCompiler
                .createPolicy(endpoints("10.0.0.1"), endpoints("10.0.0.3"),
                        actions(block), ClassifierHelper.vlan(5, 10))),
                Arrays.asList(intentCompiler.createPolicy(
                        endpoints("10.0.0.2"), endpoints("10.0.0.3"),
                        actions(allow), ClassifierHelper.vlan(11, 20)),
                        intentCompiler.createPolicy(endpoints("10.0.0.1"),
                                endpoints("10.0.0.3"), actions(block),
                                ClassifierHelper.vlan(5, 10)), intentCompiler
                                .createPolicy(endpoints("10.0.0.1"),
                                        endpoints("10.0.0.3"), actions(allow),
                                        ClassifierHelper.vlan(11, 20)),
                        intentCompiler.createPolicy(endpoints("10.0.0.2"),
                                endpoints("10.0.0.3"), actions(allow),
                                ClassifierHelper.vlan(5, 10))));
    }

}
