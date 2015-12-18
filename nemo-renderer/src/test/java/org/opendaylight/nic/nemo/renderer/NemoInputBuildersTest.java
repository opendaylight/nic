/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.nemo.renderer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.nic.nemo.renderer.NEMOIntentParser.BandwidthOnDemandParameters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.StructureStyleNemoUpdateInput;

public class NemoInputBuildersTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetUpdateBuilder() {

        LocalTime startTime = NEMOIntentParser.parseTime(NEMOIntentParserTest.START_TIME);
        Period duration = NEMOIntentParser.parsePeriod(NEMOIntentParserTest.DURATION);
        BandwidthOnDemandParameters params = new BandwidthOnDemandParameters(NEMOIntentParserTest.FROM,
                NEMOIntentParserTest.TO, NEMOIntentParserTest.BANDWIDTH, startTime, duration);
        StructureStyleNemoUpdateInput input = NemoInputBuilders.getUpdateBuilder(params).build();
        assertNotNull("Expected valid input", input);

        assertEquals(2, input.getObjects().getNode().size());
        assertEquals(NEMOIntentParserTest.FROM, input.getObjects().getNode().get(0).getNodeName().getValue());
        assertEquals(NEMOIntentParserTest.TO, input.getObjects().getNode().get(1).getNodeName().getValue());
        assertEquals(1, input.getObjects().getConnection().size());
        assertEquals(1, input.getOperations().getOperation().size());
        assertEquals(1, input.getOperations().getOperation().get(0).getAction().size());
        assertEquals(1, input.getOperations().getOperation().get(0).getAction().get(0).getParameterValues()
                .getStringValue().size());
        assertEquals(NEMOIntentParserTest.BANDWIDTH, input.getOperations().getOperation().get(0).getAction().get(0)
                .getParameterValues().getStringValue().get(0).getValue());
    }
}
