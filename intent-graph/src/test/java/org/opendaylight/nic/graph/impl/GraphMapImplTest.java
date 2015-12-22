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

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GraphMapImplTest {
    protected IntentMappingService intentMappingService;

    private GraphMapImpl service;

    @Before
    public void setUp() throws Exception {
        //this.service = spy(service);
        this.intentMappingService = mock(IntentMappingService.class);
        service = new GraphMapImpl(intentMappingService);
    }

    //create a simple tree and test that each one is initialized properly with the right parent/children pairs.
    //TODO: complete this test case
    @Test
    public final void testCreateGraph() throws Exception {
        service = spy(service);
        PowerMockito.mockStatic(FrameworkUtil.class);
        BundleContext mockBundleContext = mock(BundleContext.class);
        ServiceRegistration<GraphMapImpl> intentServiceMock = mock(ServiceRegistration.class);

        //doReturn(mockBundleContext).when(GraphMapImpl.class);
        when(mockBundleContext.registerService(GraphMapImpl.class, service, null))
                .thenReturn(intentServiceMock);
	/* TODO: Completion after the merge of IntentServiceMapping implementation */
        //service.addLabelChild("apps", "Tnt", "app1");
        //service.addLabelChildren("Tnt", "pga_label_tree", new String[]{"Dpts", "apps"}); //make null first
        //service.addLabelChildren("Dpts", "Tnt", new String[]{"IT", "Engg"});
        //service.addLabelChildren("app1", "apps", new String[]{"Web", "DB"});
        //service.add("IT", "Dpts");
        //service.add("Eng", "Dpts");
        //service.add("web", "App1");
        //service.add("db", "App1");
        //System.out.println(service.intentMappingService.get("Tnt")); //get parent and children information
        //System.out.println(service.intentMappingService.get("Dpts"));
        //System.out.println(service.intentMappingService.get("apps"));
    }
}
