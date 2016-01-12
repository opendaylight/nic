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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@PrepareForTest({LinkUpImpl.class})
@RunWith(PowerMockRunner.class)
/**
 * Created by yrineu on 12/01/16.
 */
public class LinkUpImplTest {

    private LinkUpImpl linkUpImplMock;

    private static final String MAC_ADDRESS_STR = "60:6c:66:8b:bb:e5";

    private static final String PORT_NAME = "eth0";

    private static final String NODE_CONNECTOR_ID_STR = UUID.randomUUID().toString();

    private NodeConnectorId nodeConnectorId;

    @Before
    public void setUp() {
        MacAddress macAddress = MacAddress.getDefaultInstance(MAC_ADDRESS_STR);
        nodeConnectorId = NodeConnectorId.getDefaultInstance(NODE_CONNECTOR_ID_STR);
        linkUpImplMock = PowerMockito.spy(new LinkUpImpl(macAddress, PORT_NAME, nodeConnectorId));
    }

    @Test
    public void testMacAddressNotNull() {
        assertNotNull(linkUpImplMock.getMac());
        assertEquals(linkUpImplMock.getMac().getValue(), MAC_ADDRESS_STR);
    }

    @Test
    public void testNodeConnectorIdNotNull() {
        assertNotNull(linkUpImplMock.getNodeConnectorId());
        assertEquals(linkUpImplMock.getNodeConnectorId().getValue(), NODE_CONNECTOR_ID_STR);
    }

    @Test
    public void testPortNameNotNull() {
        assertNotNull(linkUpImplMock.getPortName());
        assertEquals(linkUpImplMock.getPortName(), PORT_NAME);
    }

    @Test
    public void testTimeStampNotNull() {
        assertNotNull(linkUpImplMock.getTimeStamp());
    }
}
