/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opendaylight.nic.listeners.api.TopologyLinkUp;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.LinkId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@PrepareForTest({TopologyLinkUpImpl.class})
@RunWith(PowerMockRunner.class)
/**
 * Created by yrineu on 12/01/16.
 */
public class TopologyLinkUpImplTest {

    private TopologyLinkUpImpl topologyLinkUp;

    @Mock
    private Link linkMock;

    @Mock
    private LinkId linkIdMock;

    @Before
    public void setUp() {
        topologyLinkUp = PowerMockito.spy(new TopologyLinkUpImpl(linkMock, linkIdMock));
    }

    @Test
    public void testVerifyParameters() {
        assertNotNull(topologyLinkUp.getTimeStamp());
        assertEquals(linkMock, topologyLinkUp.getLink());
        assertEquals(linkIdMock, topologyLinkUp.getLinkId());

        assertTrue(topologyLinkUp instanceof TopologyLinkUp);
    }
}
