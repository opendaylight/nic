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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.nic.rpc.exception.JuniperModelNotSupportedException;
import org.opendaylight.nic.rpc.model.juniper.information.ForwardingClass;
import org.opendaylight.nic.rpc.model.juniper.information.StatisticsQueue;
import org.opendaylight.nic.rpc.utils.RESTUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by yrineu on 17/07/17.
 */
@PrepareForTest(StatisticsQueue.class)
@RunWith(PowerMockRunner.class)
public class StatisticsQueueTest {

    private StatisticsQueue statisticsQueue;
    private JsonNode queueElements;

    @Before
    public void setup() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode node = objectMapper.readTree(getMockResultAsString());
        final JsonNode queue = node.elements().next();

        statisticsQueue = StatisticsQueue.getInstanceBy(queue);
        queueElements = queue.elements().next();
    }
    @Test
    public void testJsonToStaticQueueDesSerialization() throws IOException {
        assertEquals(ForwardingClass.BEST_EFFORT, statisticsQueue.getForwardingClass());
        assertEquals(RESTUtils.extractIntData(queueElements.get("queue-number")),
                statisticsQueue.getQueueNumber());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-queued-packets")),
                statisticsQueue.getQueuedPackets());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-queued-packets-rate")),
                statisticsQueue.getQueuedPacketsRate());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-queued-bytes")),
                statisticsQueue.getQueuedBytes());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-queued-bytes-rate")),
                statisticsQueue.getQueuedBytesRate());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-trans-packets")),
                statisticsQueue.getTransPackets());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-trans-packets-rate")),
                statisticsQueue.getTransPacketRate());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-trans-bytes")),
                statisticsQueue.getTransBytes());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-trans-bytes-rate")),
                statisticsQueue.getTransBytesRate());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-tail-drop-packets-na")),
                statisticsQueue.getTailDropPacketsNa());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-rate-limit-drop-packets")),
                statisticsQueue.getRateLimitDropPackets());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-rate-limit-drop-packets-rate")),
                statisticsQueue.getRateLimitDropPacketsRate());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-rate-limit-drop-bytes")),
                statisticsQueue.getRateLimitDropBytes());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-rate-limit-drop-bytes-rate")),
                statisticsQueue.getRateLimitDropBytesRate());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-total-drop-packets")),
                statisticsQueue.getTotalDropPackets());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-total-drop-bytes")),
                statisticsQueue.getTotalDropBytes());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-rate-limit-drop-bytes-rate")),
                statisticsQueue.getRateLimitDropBytesRate());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-total-drop-bytes-rate")),
                statisticsQueue.getTotalDropBytesRate());
        assertEquals(RESTUtils.extractLongData(queueElements.get("queue-counters-total-drop-packets-rate")),
                statisticsQueue.getTotalDropPacketsRate());
    }

    @Test (expected = JuniperModelNotSupportedException.class)
    public void testWithInvalidValue() {
        final JsonNode emptyJson = mock(JsonNode.class);

        StatisticsQueue.getInstanceBy(emptyJson);
    }

    private String getMockResultAsString() {
        return "{\"queue\":[{\"queue-number\":[{\"data\":\"1\"}],\"forwarding-class-name\":[{\"data\":\"best-effort\"}]" +
                ",\"queue-counters-queued-packets\":[{\"data\":\"2\"}],\"queue-counters-queued-packets-rate\":" +
                "[{\"data\":\"3\"}],\"queue-counters-queued-bytes\":[{\"data\":\"4\"}]" +
                ",\"queue-counters-queued-bytes-rate\":[{\"data\":\"5\"}],\"queue-counters-trans-packets\":" +
                "[{\"data\":\"6\"}],\"queue-counters-trans-packets-rate\":[{\"data\":\"7\"}]" +
                ",\"queue-counters-trans-bytes\":[{\"data\":\"8\"}],\"queue-counters-trans-bytes-rate\":" +
                "[{\"data\":\"9\"}],\"queue-counters-tail-drop-packets-na\":[{\"data\":\"Not Available\"}]" +
                ",\"queue-counters-rate-limit-drop-packets\":[{\"data\":\"10\"}]" +
                ",\"queue-counters-rate-limit-drop-packets-rate\":" +
                "[{\"data\":\"11\"}],\"queue-counters-rate-limit-drop-bytes\":[{\"data\":\"12\"}]" +
                ",\"queue-counters-rate-limit-drop-bytes-rate\":[{\"data\":\"13\"}]" +
                ",\"queue-counters-total-drop-packets\":[{\"data\":\"14\"}]" +
                ",\"queue-counters-total-drop-packets-rate\":[{\"data\":\"15\"}]" +
                ",\"queue-counters-total-drop-bytes\":[{\"data\":\"16\"}]" +
                ",\"queue-counters-total-drop-bytes-rate\":[{\"data\":\"17\"}]}]}";
    }
}
