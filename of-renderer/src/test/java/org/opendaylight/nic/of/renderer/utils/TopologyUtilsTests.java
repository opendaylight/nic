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
