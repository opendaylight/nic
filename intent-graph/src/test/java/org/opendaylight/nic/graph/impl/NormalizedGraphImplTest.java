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
import org.opendaylight.nic.graph.api.InputGraph;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;

import java.util.Collection;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class NormalizedGraphImplTest {
    protected IntentMappingService mocklabelRelationMap;
    protected Collection<InputGraph> mockGraph = new HashSet<>();

    private NormalizedGraphImpl service;

    @Before
    public void setUp() throws Exception {
        this.mocklabelRelationMap = mock(org.opendaylight.nic.mapping.api.IntentMappingService.class);
        this.mockGraph.add(mock(org.opendaylight.nic.graph.impl.InputGraphImpl.class));
        service = new NormalizedGraphImpl(mocklabelRelationMap);
    }

    @Test
    public final void testNormalizedGraph() throws Exception {
        service = spy(service);
        PowerMockito.mockStatic(FrameworkUtil.class);
        BundleContext mockBundleContext = mock(BundleContext.class);
        ServiceRegistration<NormalizedGraphImpl> intentServiceMock = mock(ServiceRegistration.class);

        when(mockBundleContext.registerService(NormalizedGraphImpl.class, service, null))
                .thenReturn(intentServiceMock);
        Collection<InputGraph> normalizedGraph = service.normalizedGraph(mockGraph);
        assertThat(normalizedGraph, instanceOf(Collection.class));
    }
}
