/*
 * Copyright © 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.utils;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;

public class TopologyUtilsTests {

    @Test
    public void testextractTopologyNodeId(){
        String nodeConnectorId = "openflow2:1";

        org.opendaylight.yang.gen.v1.urn
                .tbd.params.xml.ns.yang.network
                .topology.rev131021.NodeId nodeId = TopologyUtils.extractTopologyNodeId(nodeConnectorId);

        assertNotNull(nodeId);
    }
}
