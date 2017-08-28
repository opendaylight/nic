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
import org.opendaylight.nic.rpc.model.juniper.information.QueueCounters;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static org.mockito.Mockito.mock;

/**
 * Created by yrineu on 18/07/17.
 */
@PrepareForTest(QueueCounters.class)
@RunWith(PowerMockRunner.class)
public class QueueCountersTest {

    private QueueCounters queueCounters;
    private JsonNode queueCountersNode;

    @Before
    public void setup() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode node = objectMapper.readTree(getMockResultAsString());
        queueCountersNode = node.elements().next();

        queueCounters = QueueCounters.getInstanceFrom(queueCountersNode);
    }

    @Test
    public void testNotNull() {
        Assert.assertNotNull(queueCounters.getStatisticsQueue());
        Assert.assertNotNull(queueCounters.getInterfaceCosSummary());
    }

    @Test(expected = JuniperModelNotSupportedException.class)
    public void testWithInvalidJson() {
        final JsonNode emptyNode = mock(JsonNode.class);
        QueueCounters.getInstanceFrom(emptyNode);
    }

    public String getMockResultAsString() {
        return "{\"queue-counters\":[{\"attributes\":{\"junos:style\":\"detail\"},\"interface-cos-summary\":" +
                "[{\"intf-cos-forwarding-classes-supported\":[{\"data\":\"16\"}]" +
                ",\"intf-cos-forwarding-classes-in-use\":[{\"data\":\"5\"}],\"intf-cos-queue-type\":" +
                "[{\"data\":\"Egress queues\"}],\"intf-cos-num-queues-supported\":[{\"data\":\"12\"}]" +
                ",\"intf-cos-num-queues-in-use\":[{\"data\":\"5\"}]}],\"queue\":[{\"queue-number\":[{\"data\":\"0\"}]" +
                ",\"forwarding-class-name\":[{\"data\":\"best-effort\"}],\"queue-counters-queued-packets\"" +
                ":[{\"data\":\"0\"}],\"queue-counters-queued-packets-rate\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-queued-bytes\":[{\"data\":\"0\"}],\"queue-counters-queued-bytes-rate\":" +
                "[{\"data\":\"0\"}],\"queue-counters-trans-packets\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-trans-packets-rate\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-trans-bytes\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-trans-bytes-rate\":[{\"data\":\"0\"}],\"queue-counters-tail-drop-packets-na\"" +
                ":[{\"data\":\"Not Available\"}],\"queue-counters-rate-limit-drop-packets\"" +
                ":[{\"data\":\"0\"}],\"queue-counters-rate-limit-drop-packets-rate\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-rate-limit-drop-bytes\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-rate-limit-drop-bytes-rate\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-total-drop-packets\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-total-drop-packets-rate\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-total-drop-bytes\":[{\"data\":\"0\"}]" +
                ",\"queue-counters-total-drop-bytes-rate\":[{\"data\":\"0\"}]}]}]}";
    }
}
