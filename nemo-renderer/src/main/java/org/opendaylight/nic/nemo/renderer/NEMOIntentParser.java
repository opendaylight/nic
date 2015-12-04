/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.nemo.renderer;

import java.util.List;

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

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

/**
 *
 * @author gwu
 *
 */
public class NEMOIntentParser {

    public static NEMOData parseForBandwidthOnDemand(Intent intent) {

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
                String bandwidth = constraint.getBandwidthConstraint().getBandwidth();
                String startTime = condition.getDaily().getStartTime().getValue();
                String duration = condition.getDaily().getDuration().getValue();

                return new NEMOData(from, to, bandwidth, startTime, duration);
            }

        }

        return null;

    }
}
