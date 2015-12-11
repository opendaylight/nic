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

import java.util.Collection;

import static org.mockito.Mockito.spy;

@RunWith(MockitoJUnitRunner.class)
public class GraphMapImplTest {
    private GraphMapImpl service = new GraphMapImpl();

    @Before
    public void setUp() throws Exception {
    }


    //create a simple tree and test that each one is initialized properly with the right parent/children pairs.
    //TODO: complete this test case
    @Test
    public final void testCreateGraph() {
        service = spy(service);
        service.add("Tnt", null, (String)null); //make null first
        service.add("Dpts", "Tnt", (String)null);
        service.add("apps", "Tnt", (String)null);
        service.add("App1", "apps");
        service.add("IT", "Dpts");
        service.add("Eng", "Dpts");
        service.add("web", "App1");
        service.add("db", "App1");
        System.out.println(service.getMultiMap().keySet()); // print list of keys
        for (String key : service.getMultiMap().keySet()) {
            Collection<LabelImpl> parentLabels = service.getMultiMap().get(key);
            for (LabelImpl parent: parentLabels)
                System.out.println(parent.getChildren());


        }
    }

}
