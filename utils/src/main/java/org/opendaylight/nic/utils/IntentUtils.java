/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.utils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class IntentUtils {

    private static final Logger LOG = LoggerFactory.getLogger(IntentUtils.class);

    private static final int NUM_OF_SUPPORTED_ACTION = 1;
    private static final int  NUM_OF_SUPPORTED_EPG = 2;

    private IntentUtils() {
    }

    public static boolean validateMAC(String mac) {
        if (mac == null || mac.isEmpty()) {
            return false;
        }
        Pattern macPattern = Pattern.compile("([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}");
        Matcher macMatcher = macPattern.matcher(mac);
        return macMatcher.matches();
    }

    public static boolean validateIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        ip = ip.trim();
        if ((ip.length() < 6) & (ip.length() > 15)) {
            return false;
        }

        try {
            Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
            Matcher matcher = pattern.matcher(ip);
            return matcher.matches();
        } catch (PatternSyntaxException ex) {
            LOG.error("IP Pattern not matched");
            return false;
        }
    }

    public static boolean verifyIntent(Intent intent) {
        boolean result = false;
        if (intent != null && intent.getId() != null) {
            if (verifyIntentActions(intent)) {
                result = verifyIntentSubjects(intent);
            }
        } else {
            LOG.warn("Intent ID is not specified {}", intent);
        }
        return result;
    }

    public static boolean verifyIntentActions(Intent intent) {
        boolean result = true;
        List<Actions> actions = intent.getActions();
        if (actions == null || actions.size() > NUM_OF_SUPPORTED_ACTION) {
            LOG.warn("Intent's action is either null or there is more than {} action {}", NUM_OF_SUPPORTED_ACTION,
                    intent);
            result = false;
        }
        return result;
    }

    public static boolean verifyIntentSubjects(Intent intent) {
        boolean result = true;
        List<Subjects> subjects = intent.getSubjects();
        if (subjects == null || subjects.size() > NUM_OF_SUPPORTED_EPG) {
            LOG.warn("Intent's subjects is either null or there is more than {} subjects {}", NUM_OF_SUPPORTED_EPG,
                    intent);
            result = false;
        }

        return result;
    }

    // TODO: Use Mapping service to resolve the subjects
    // Retrieve the end points
    public static List<String> extractEndPointGroup(Intent intent) {
        final Uuid uuid = intent.getId();
        final List<Subjects> listSubjects = intent.getSubjects();
        final String[] endPointGroups = new String[listSubjects.size()];

        for (Subjects subjects : listSubjects) {
            Subject subject = subjects.getSubject();
            int order = subjects.getOrder();
            verifySubjectInstance(subject, uuid);
            EndPointGroup endPointGroup = (EndPointGroup) subject;

            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group
                .EndPointGroup epg = endPointGroup.getEndPointGroup();

            if (epg != null) {
                endPointGroups[order-1] = epg.getName();
            }
        }
        return Arrays.asList(endPointGroups);
    }

    public static void verifySubjectInstance(Subject subject, Uuid intentId) {
        if (!(subject instanceof EndPointGroup)
                && !(subject instanceof EndPointSelector)
                && !(subject instanceof EndPointGroupSelector)) {
            LOG.info("Subject is not specified: {}", intentId);
        }
    }
}