/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.nemo.renderer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.nic.nemo.renderer.NEMOIntentParser.BandwidthOnDemandParameters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.conditions.rev150122.Duration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.conditions.rev150122.TimeOfDay;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.ActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.ConditionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.ConstraintsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.SubjectsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.AllowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.conditions.condition.Daily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.conditions.condition.DailyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.BandwidthConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.BandwidthConstraintBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.StructureStyleNemoUpdateInputBuilder;

/**
 * @author gwu
 *
 */
public class NEMOIntentParserTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.nemo.renderer.NEMOIntentParser#parseBandwidthOnDemand(org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent)}
     * .
     */
    @Test
    public void testParseBandwidthOnDemand() {

        Intent emptyIntent = new IntentBuilder().build();
        try {
            NEMOIntentParser.parseBandwidthOnDemand(emptyIntent);
            fail("Did not throw expected NullPointerException");
        } catch (NullPointerException expectedException) {
        }

        Intent intent = getBandwidthOnDemandIntent();
        BandwidthOnDemandParameters params = NEMOIntentParser.parseBandwidthOnDemand(intent);
        StructureStyleNemoUpdateInputBuilder inputBuilder = NemoInputBuilders.getUpdateBuilder(params);
        assertNotNull("Expected valid inputBuilder", inputBuilder);
    }

    public static final String FROM = "developers";
    public static final String TO = "marketing";
    public static final String BANDWIDTH = "10G";
    public static final String START_TIME = "08:00:00Z";
    public static final String DURATION = "1h";

    public static Intent getBandwidthOnDemandIntent() {
        IntentBuilder b = new IntentBuilder();
        b.setActions(Arrays.asList(new ActionsBuilder().setAction(new AllowBuilder().build()).build()));

        Subjects from = subject((short) 1, FROM);
        Subjects to = subject((short) 2, TO);
        b.setSubjects(Arrays.asList(from, to));

        BandwidthConstraint constraint = bandwidth(BANDWIDTH);
        b.setConstraints(Arrays.asList(new ConstraintsBuilder().setOrder((short) 1).setConstraints(constraint).build()));

        Daily condition = daily(START_TIME, DURATION);
        b.setConditions(Arrays.asList(new ConditionsBuilder().setOrder((short) 1).setCondition(condition).build()));

        return b.build();
    }

    private static Subjects subject(short order, String name) {
        return new SubjectsBuilder()
                .setSubject(
                        new EndPointGroupBuilder()
                                .setEndPointGroup(
                                        new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroupBuilder()
                                                .setName(name).build()).build()).setOrder(order).build();
    }

    private static BandwidthConstraint bandwidth(String bandwidth) {
        return new BandwidthConstraintBuilder()
                .setBandwidthConstraint(
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.bandwidth.constraint.BandwidthConstraintBuilder()
                                .setBandwidth(BANDWIDTH).build()).build();
    }

    private static Daily daily(String startTime, String duration) {
        return new DailyBuilder()
                .setDaily(
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.conditions.condition.daily.DailyBuilder()
                                .setStartTime(new TimeOfDay(startTime)).setDuration(new Duration(duration)).build())
                .build();
    }

}
