/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.nemo.renderer;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.concurrent.Immutable;

import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.format.ISOPeriodFormat;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Conditions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.conditions.condition.Daily;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.BandwidthConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

/**
 *
 * @author gwu
 *
 */
public class NEMOIntentParser {

    private static final Logger LOG = LoggerFactory.getLogger(NEMOIntentParser.class);

    private NEMOIntentParser() {
    }

    @Immutable
    public static class BandwidthOnDemandParameters {
        public final String from;
        public final String to;
        public final long bandwidth;
        public final LocalTime startTime;
        public final Period duration;

        public BandwidthOnDemandParameters(String from, String to, long bandwidth, LocalTime startTime, Period duration) {
            super();
            this.from = from;
            this.to = to;
            this.bandwidth = bandwidth;
            this.startTime = startTime;
            this.duration = duration;
        }
    }

    private static final Pattern BANDWIDTH_PATTERN = Pattern.compile("(\\d+)([kmgt]?)", Pattern.CASE_INSENSITIVE);

    /**
     * Simple parsing of strings like "10G" bps into a long value in number of kbps.
     *
     * @param bandwidthStr
     * @return number of kbps
     */
    public static long parseBandwidthString(String bandwidthStr) {
        Matcher matcher = BANDWIDTH_PATTERN.matcher(bandwidthStr);
        if (matcher.matches()) {
            BigDecimal num = new BigDecimal(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            final BigDecimal KILO = new BigDecimal(1024L);
            if (unit.equals("k")) {
                num = num.multiply(KILO);
            } else if (unit.equals("m")) {
                num = num.multiply(KILO.pow(2));
            } else if (unit.equals("g")) {
                num = num.multiply(KILO.pow(3));
            } else if (unit.equals("t")) {
                num = num.multiply(KILO.pow(4));
            }
            // divide to get kbps
            return num.divide(KILO, BigDecimal.ROUND_CEILING).longValueExact();
        } else {
            NumberFormatException exception = new NumberFormatException("Invalid bandwidth string " + bandwidthStr
                    + "; must be of pattern " + BANDWIDTH_PATTERN.pattern());
            LOG.warn(exception.getMessage(), exception);
            throw exception;
        }
    }

    public static BandwidthOnDemandParameters parseBandwidthOnDemand(Intent intent) {

        List<Actions> actions = intent.getActions();
        Action action = !actions.isEmpty() ? actions.get(0).getAction() : null;

        // subjects in sorted order
        List<Subjects> subjects = Ordering.natural().onResultOf(new Function<Subjects, Short>() {
            @Override
            public Short apply(Subjects input) {
                return input.getOrder();
            }
        }).immutableSortedCopy(intent.getSubjects());

        BandwidthConstraint constraint = null;
        for (Constraints c : intent.getConstraints()) {
            if (c.getConstraints() instanceof BandwidthConstraint) {
                constraint = (BandwidthConstraint) c.getConstraints();
            }
        }

        List<Conditions> conditions = intent.getConditions();
        Daily condition = null;
        for (Conditions c : conditions) {
            if (c.getCondition() instanceof Daily) {
                condition = (Daily) c.getCondition();
            }
        }

        if (action instanceof Allow && constraint instanceof BandwidthConstraint && condition instanceof Daily
                && subjects.size() == 2) {

            if (subjects.get(0).getSubject() instanceof EndPointGroup
                    && subjects.get(1).getSubject() instanceof EndPointGroup) {

                String from = ((EndPointGroup) subjects.get(0).getSubject()).getEndPointGroup().getName();
                String to = ((EndPointGroup) subjects.get(1).getSubject()).getEndPointGroup().getName();
                String bandwidthStr = constraint.getBandwidthConstraint().getBandwidth();
                long bandwidth = parseBandwidthString(bandwidthStr);

                String startTimeStr = condition.getDaily().getStartTime().getValue();
                LocalTime startTime = parseTime(startTimeStr);

                String durationStr = condition.getDaily().getDuration().getValue();
                Period duration = parsePeriod(durationStr);

                // return new BandwidthOnDemandParameters(from, to, bandwidth, startTime, duration);
                return new BandwidthOnDemandParameters(from, to, bandwidth, startTime, duration);
            }

        }

        return null;

    }

    /**
     * parse start time as ISO8601 time string
     *
     * @param startTimeStr
     * @return
     */
    public static LocalTime parseTime(String startTimeStr) {
        return ISODateTimeFormat.timeNoMillis().parseLocalTime(startTimeStr);
    }

    /**
     * parse duration as ISO8601 duration string
     *
     * @param durationStr
     * @return
     */
    public static Period parsePeriod(String durationStr) {
        return ISOPeriodFormat.standard().parsePeriod("PT" + durationStr);
    }

}
