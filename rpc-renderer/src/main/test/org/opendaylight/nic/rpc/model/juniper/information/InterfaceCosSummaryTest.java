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
import org.opendaylight.nic.rpc.model.juniper.information.InterfaceCosSummary;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.opendaylight.nic.rpc.utils.RESTUtils.extractIntData;
import static org.opendaylight.nic.rpc.utils.RESTUtils.extractStringData;

/**
 * Created by yrineu on 18/07/17.
 */
@PrepareForTest(InterfaceCosSummary.class)
@RunWith(PowerMockRunner.class)
public class InterfaceCosSummaryTest {

    private InterfaceCosSummary interfaceCosSummary;
    private JsonNode summaryElements;

    @Before
    public void setup() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode node = objectMapper.readTree(getMockResultAsString());
        final JsonNode summaryNode = node.elements().next();

        summaryElements = summaryNode.elements().next();
        interfaceCosSummary = InterfaceCosSummary.getInstanceBy(summaryNode);
    }

    @Test
    public void testJsonToStaticInterfaceCosSummaryDesSerialization() {
        System.out.println(summaryElements.get("intf-cos-forwarding-classes-supported"));
        Assert.assertEquals(extractIntData(summaryElements.get("intf-cos-forwarding-classes-supported")),
                interfaceCosSummary.getForwardingClassesSupported());
        Assert.assertEquals(extractIntData(summaryElements.get("intf-cos-forwarding-classes-in-use")),
                interfaceCosSummary.getForwardingClassesInUs());

        Assert.assertEquals(extractStringData(summaryElements.get("intf-cos-queue-type")),
                interfaceCosSummary.getQueueType());

        Assert.assertEquals(extractIntData(summaryElements.get("intf-cos-num-queues-supported")),
                interfaceCosSummary.getNumQueuesSupported());
        Assert.assertEquals(extractIntData(summaryElements.get("intf-cos-num-queues-in-use")),
                interfaceCosSummary.getNumQueuesInUse());
    }

    @Test(expected = JuniperModelNotSupportedException.class)
    public void testWithInvalidValue() {
        final JsonNode emptyNode = mock(JsonNode.class);

        InterfaceCosSummary.getInstanceBy(emptyNode);
    }


    public String getMockResultAsString() {
        return "{\"interface-cos-summary\":[{\"intf-cos-forwarding-classes-supported\":[{\"data\":\"16\"}]" +
                ",\"intf-cos-forwarding-classes-in-use\":[{\"data\":\"5\"}],\"intf-cos-queue-type\":" +
                "[{\"data\":\"Egress queues\"}],\"intf-cos-num-queues-supported\":" +
                "[{\"data\":\"12\"}],\"intf-cos-num-queues-in-use\":[{\"data\":\"5\"}]}]}";
    }
}
