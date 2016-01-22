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
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RelationMapImplTest {
    protected IntentMappingService mocklabelRelationMap;

    private RelationMapImpl service;

    @Before
    public void setUp() throws Exception {
        this.mocklabelRelationMap = mock(IntentMappingService.class);
        service = new RelationMapImpl(mocklabelRelationMap);
    }

    @Test
    public final void testAddLabelRelation() throws Exception {
        boolean actualResult, expectedResult;
        service = spy(service);
        PowerMockito.mockStatic(FrameworkUtil.class);
        BundleContext mockBundleContext = mock(BundleContext.class);
        ServiceRegistration<RelationMapImpl> intentServiceMock = mock(ServiceRegistration.class);

        when(mockBundleContext.registerService(RelationMapImpl.class, service, null))
                .thenReturn(intentServiceMock);
        expectedResult = true;
        actualResult = service.addLabelRelation("parent", "child");
        assertEquals(expectedResult, actualResult);
    }
}
