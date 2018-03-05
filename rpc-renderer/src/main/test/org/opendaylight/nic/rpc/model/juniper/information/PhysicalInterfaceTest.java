/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.model.juniper.information;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.nic.rpc.exception.JuniperModelNotSupportedException;
import org.opendaylight.nic.rpc.model.juniper.information.InterfaceStatus;
import org.opendaylight.nic.rpc.model.juniper.information.PhysicalInterface;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import static org.mockito.Mockito.mock;

/**
 * Created by yrineu on 18/07/17.
 */
@PrepareForTest(PhysicalInterface.class)
@RunWith(PowerMockRunner.class)
public class PhysicalInterfaceTest {

    private Set<PhysicalInterface> physicalInterfaces;

    @Before
    public void setup() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode node = objectMapper.readTree(getMockResultAsString());
        final Iterator<JsonNode> iterator = node.elements();

        final JsonNode nextNode = iterator.next();

        physicalInterfaces = PhysicalInterface.getInstanceBy(nextNode);

    }

    @Test
    public void testNotNullValues() {
        physicalInterfaces.forEach(physicalInterface -> {
            Assert.assertEquals(InterfaceStatus.up, physicalInterface.getAdminStatus());
            Assert.assertEquals(InterfaceStatus.down, physicalInterface.getOperStatus());
            Assert.assertEquals("xe-0/0/0", physicalInterface.getName());

            Assert.assertNotNull(physicalInterface.getAdminStatus());
            Assert.assertNotNull(physicalInterface.getQueueCounters());
        });
    }

    @Test(expected = JuniperModelNotSupportedException.class)
    public void testWithInvalidJson() {
        final JsonNode emptyNode = mock(JsonNode.class);
        PhysicalInterface.getInstanceBy(emptyNode);
    }

    public String getMockResultAsString() {
        return "{\"physical-interface\":[{},{},{},{\"name\":[{\"data\":\"xe-0/0/0\"}],\"admin-status\":[{\"data\":" +
                "\"up\",\"attributes\":{\"junos:format\":\"Enabled\"}}],\"oper-status\":[{\"data\":\"down\"}]" +
                ",\"local-index\":[{\"data\":\"649\"}],\"snmp-index\":[{\"data\":\"511\"}],\"queue-counters\":" +
                "[{\"attributes\":{\"junos:style\":\"detail\"},\"interface-cos-summary\":" +
                "[{\"intf-cos-forwarding-classes-supported\":[{\"data\":\"16\"}]" +
                ",\"intf-cos-forwarding-classes-in-use\":[{\"data\":\"5\"}],\"intf-cos-queue-type\":" +
                "[{\"data\":\"Egress queues\"}],\"intf-cos-num-queues-supported\":[{\"data\":\"12\"}]" +
                ",\"intf-cos-num-queues-in-use\":[{\"data\":\"5\"}]}],\"queue\":[{\"queue-number\":" +
                "[{\"data\":\"0\"}],\"forwarding-class-name\":[{\"data\":\"best-effort\"}]" +
                ",\"queue-counters-queued-packets\":[{\"data\":\"0\"}],\"queue-counters-queued-packets-rate\":" +
                "[{\"data\":\"0\"}],\"queue-counters-queued-bytes\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-queued-bytes-rate\":[{\"data\":\"0\"}],\"queue-counters-trans-packets\":" +
                "[{\"data\":\"0\"}],\"queue-counters-trans-packets-rate\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-trans-bytes\":[{\"data\":\"0\"}],\"queue-counters-trans-bytes-rate\":" +
                "[{\"data\":\"0\"}],\"queue-counters-tail-drop-packets-na\":[{\"data\":\"Not Available\"}]" +
                ",\"queue-counters-rate-limit-drop-packets\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-rate-limit-drop-packets-rate\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-rate-limit-drop-bytes\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-rate-limit-drop-bytes-rate\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-total-drop-packets\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-total-drop-packets-rate\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-total-drop-bytes\":[{\"data\":\"0\"}],\"queue-counters-total-drop-bytes-rate\":" +
                "[{\"data\":\"0\"}]}]}]},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}]}";
    }
}
