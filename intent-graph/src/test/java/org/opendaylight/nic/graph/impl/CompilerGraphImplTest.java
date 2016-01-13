/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.nic.graph.api.CompilerGraph;
import org.opendaylight.nic.graph.api.CompilerGraphFactory;
import org.opendaylight.nic.graph.api.InputGraph;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.ActionTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.EdgeTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Edges;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.EdgesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.graph.rev150911.graph.NodesBuilder;

import java.net.UnknownHostException;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class CompilerGraphImplTest {
    CompilerGraph intentCompiler;
    Edges allow;
    Edges block;
    Edges redirect;
    Edges monitor;
    protected IntentMappingService intentMappingService;

    private Set<Nodes> endpoints(String... hosts)
            throws UnknownHostException {
        Set<Nodes> nodes = new LinkedHashSet<>();

        for (String label : hosts) {
            nodes.add(new NodesBuilder().setName(label).build());
        }
        return nodes;
    }

    private Set<Edges> actions(Edges... actions) {
        Set<Edges> actionSet = new LinkedHashSet<>();
        for (Edges action : actions) {
            actionSet.add(action);
        }
        return actionSet;
    }

    private void testCompile(Collection<InputGraph> input, Collection<InputGraph> output)
            throws Exception {
        Collection<InputGraph> compiledPolicies = intentCompiler.compile(input);
        assertNotNull(compiledPolicies);
        assertEquals(output.size(), compiledPolicies.size());
        assertTrue(compiledPolicies.containsAll(output));
    }

    @Before
    public void setUp() throws Exception {
        this.intentMappingService = mock(IntentMappingService.class);
        intentCompiler = CompilerGraphFactory.createGraphCompiler();
        allow = new EdgesBuilder().setType(EdgeTypes.MustAllow).build();
        block = new EdgesBuilder().setType(EdgeTypes.MustDeny).setActionType(ActionTypes.Exclusive).build();
        redirect = new EdgesBuilder().setType(EdgeTypes.CanAllow).setActionType(ActionTypes.Composable).build();
        monitor = new EdgesBuilder().setType(EdgeTypes.CanAllow).setActionType(ActionTypes.Observer).build();
    }

    @Test
    public void testEmptyCompile() throws Exception {
        testCompile(Collections.<InputGraph>emptyList(),
                Collections.<InputGraph>emptyList());
    }

    @Test
    public void testEmptyEndpointsCompile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createGraph(endpoints(),
                endpoints(), actions(allow))), Arrays.asList(intentCompiler
                .createGraph(endpoints(), endpoints(), actions(allow))));
    }

    @Test
    public void testNonConflictCompile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createGraph(
                endpoints("web"), endpoints("DB"), actions(allow)),
                intentCompiler.createGraph(endpoints("campus"),
                        endpoints("app"), actions(block))), Arrays.asList(
                intentCompiler.createGraph(endpoints("web"),
                        endpoints("DB"), actions(allow)), intentCompiler
                        .createGraph(endpoints("campus"),
                                endpoints("app"), actions(block))));

    }

    @Test
    public void testConflictCompile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createGraph(
                endpoints("web"), endpoints("DB"), actions(allow)),
                intentCompiler.createGraph(endpoints("web"),
                        endpoints("DB"), actions(block))),
                Arrays.asList(intentCompiler.createGraph(
                        endpoints("web"), endpoints("DB"),
                        actions(block))));
    }

    @Test
    public void testConflictMergeCompile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createGraph(
                endpoints("10.0.0.1", "10.0.0.2"), endpoints("10.0.0.3"),
                actions(allow)), intentCompiler.createGraph(
                endpoints("10.0.0.1"), endpoints("10.0.0.3"), actions(block))),
                Arrays.asList(intentCompiler.createGraph(
                        endpoints("10.0.0.2"), endpoints("10.0.0.3"),
                        actions(allow)), intentCompiler.createGraph(
                        endpoints("10.0.0.1"), endpoints("10.0.0.3"),
                        actions(block))));
    }

    @Test
    public void testConflictThreeCompile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createGraph(
                endpoints("10.0.0.1", "10.0.0.2"), endpoints("10.0.0.3"),
                actions(allow)), intentCompiler.createGraph(
                endpoints("10.0.0.1"), endpoints("10.0.0.3"), actions(block)),
                intentCompiler.createGraph(endpoints("10.0.0.2"),
                        endpoints("10.0.0.3"), actions(block))), Arrays.asList(
                intentCompiler.createGraph(endpoints("10.0.0.1"),
                        endpoints("10.0.0.3"), actions(block)), intentCompiler
                        .createGraph(endpoints("10.0.0.2"),
                                endpoints("10.0.0.3"), actions(block))));
    }

    @Test
    public void testConflictDestinationCompile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createGraph(
                endpoints("10.0.0.1"), endpoints("10.0.0.2", "10.0.0.3"),
                actions(allow)), intentCompiler.createGraph(
                endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(block))),
                Arrays.asList(intentCompiler.createGraph(
                        endpoints("10.0.0.1"), endpoints("10.0.0.3"),
                        actions(allow)), intentCompiler.createGraph(
                        endpoints("10.0.0.1"), endpoints("10.0.0.2"),
                        actions(block))));
    }

    @Test
    public void testConflictDestination2Compile() throws Exception {
        testCompile(Arrays.asList(
                intentCompiler.createGraph(endpoints("10.0.0.1"),
                        endpoints("10.0.0.2", "10.0.0.3"), actions(allow)),
                intentCompiler.createGraph(endpoints("10.0.0.1"),
                        endpoints("10.0.0.2", "10.0.0.5"), actions(block))),
                Arrays.asList(intentCompiler.createGraph(
                        endpoints("10.0.0.1"), endpoints("10.0.0.3"),
                        actions(allow)), intentCompiler.createGraph(
                        endpoints("10.0.0.1"), endpoints("10.0.0.2"),
                        actions(block)), intentCompiler.createGraph(
                        endpoints("10.0.0.1"), endpoints("10.0.0.5"),
                        actions(block))));
    }

    @Test
    public void testConflictMergeActionsCompile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createGraph(
                endpoints("10.0.0.1", "10.0.0.2", "10.0.0.10"),
                endpoints("10.0.0.3"), actions(allow)), intentCompiler
                        .createGraph(endpoints("10.0.0.1"),
                                endpoints("10.0.0.3", "10.0.0.20"), actions(block)),
                intentCompiler.createGraph(endpoints("10.0.0.2"),
                        endpoints("10.0.0.3"), actions(redirect)),
                intentCompiler.createGraph(endpoints("10.0.0.1"),
                        endpoints("10.0.0.20"), actions(monitor))),
                Arrays.asList(intentCompiler.createGraph(
                        endpoints("10.0.0.1"), endpoints("10.0.0.3"),
                        actions(block)), intentCompiler.createGraph(
                        endpoints("10.0.0.10"), endpoints("10.0.0.3"),
                        actions(allow)), intentCompiler.createGraph(
                        endpoints("10.0.0.2"), endpoints("10.0.0.3"),
                        actions(allow, redirect)), intentCompiler.createGraph(
                        endpoints("10.0.0.1"), endpoints("10.0.0.20"),
                        actions(block, monitor))));
    }

    @Test
    public void testClassifierNonConflicting() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createGraph(
                endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(allow),
                ClassifierHelper.vlan(10, 20)), intentCompiler.createGraph(
                endpoints("10.0.0.3"), endpoints("10.0.0.4"), actions(block),
                ClassifierHelper.vlan(10, 40))), Arrays.asList(intentCompiler
                        .createGraph(endpoints("10.0.0.1"), endpoints("10.0.0.2"),
                                actions(allow), ClassifierHelper.vlan(10, 20)),
                intentCompiler.createGraph(endpoints("10.0.0.3"),
                        endpoints("10.0.0.4"), actions(block),
                        ClassifierHelper.vlan(10, 40))));

    }

    @Test
    public void testClassifierConflictCompile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createGraph(
                endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(allow),
                ClassifierHelper.vlan(10, 20)), intentCompiler.createGraph(
                endpoints("10.0.0.1"), endpoints("10.0.0.2"), actions(block),
                ClassifierHelper.vlan(30, 40))), Arrays.asList(intentCompiler
                        .createGraph(endpoints("10.0.0.1"), endpoints("10.0.0.2"),
                                actions(allow), ClassifierHelper.vlan(10, 20)),
                intentCompiler.createGraph(endpoints("10.0.0.1"),
                        endpoints("10.0.0.2"), actions(block),
                        ClassifierHelper.vlan(30, 40))));
    }

    @Test
    public void testClassifier2Compile() throws Exception {
        testCompile(Arrays.asList(intentCompiler.createGraph(
                endpoints("10.0.0.1", "10.0.0.2"), endpoints("10.0.0.3"),
                actions(allow), ClassifierHelper.vlan(5, 20)), intentCompiler
                        .createGraph(endpoints("10.0.0.1"), endpoints("10.0.0.3"),
                                actions(block), ClassifierHelper.vlan(5, 10))),
                Arrays.asList(intentCompiler.createGraph(
                        endpoints("10.0.0.2"), endpoints("10.0.0.3"),
                        actions(allow), ClassifierHelper.vlan(11, 20)),
                        intentCompiler.createGraph(endpoints("10.0.0.1"),
                                endpoints("10.0.0.3"), actions(block),
                                ClassifierHelper.vlan(5, 10)), intentCompiler
                                .createGraph(endpoints("10.0.0.1"),
                                        endpoints("10.0.0.3"), actions(allow),
                                        ClassifierHelper.vlan(11, 20)),
                        intentCompiler.createGraph(endpoints("10.0.0.2"),
                                endpoints("10.0.0.3"), actions(allow),
                                ClassifierHelper.vlan(5, 10))));
    }
}
