/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.utils;

import org.opendaylight.nic.utils.exceptions.IntentElementNotFoundException;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.IntentsLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class IntentUtils {

    private static final String IP_PATTERN = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final Logger LOG = LoggerFactory.getLogger(IntentUtils.class);

    private static final int NUM_OF_SUPPORTED_ACTION = 1;
    private static final int  NUM_OF_SUPPORTED_EPG = 2;

    //TODO: Remove duplicated constants
    public static final Integer SRC_END_POINT_GROUP_INDEX = 0;
    public static final Integer DST_END_POINT_GROUP_INDEX = 1;

    private static final String NO_ACTION_FOUND_MESSAGE = "No action found for Intent ID: ";
    private static final String ACTION_NULL_MESSAGE = "Action does not exist for Intent with ID: ";
    private static final String INVALID_SUBJECT_MESSAGE = "Subject is not specified for Intent ID: ";
    private static final String NO_END_POINT_FOUND_MESSAGE = "No EndPoint found for Intent ID: ";
    private static final String END_POINT_NULL_MESSAGE = "EndPoint does not exist in EndPointGroups.";

    public static final InstanceIdentifier<Intents> INTENTS_IID = InstanceIdentifier.builder(Intents.class).build();
    public static final InstanceIdentifier<IntentsLimiter> INTENTS_LIMITER_IDD = InstanceIdentifier.builder(IntentsLimiter.class).build();

    private IntentUtils() {
    }

    public static boolean validateMAC(final String mac) {
        if (mac == null || mac.isEmpty()) {
            return false;
        }
        final Pattern macPattern = Pattern.compile("([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}");
        final Matcher macMatcher = macPattern.matcher(mac);
        return macMatcher.matches();
    }

    public static boolean validateIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        ip = ip.trim();

        if ((ip.length() < 7) || (ip.length() > 15)) {
            return false;
        }

        try {
            Pattern pattern = Pattern.compile(IP_PATTERN);
            Matcher matcher = pattern.matcher(ip);
            return matcher.matches();
        } catch (PatternSyntaxException ex) {
            LOG.error("IP Pattern not matched");
            return false;
        }
    }

    public static boolean verifyIntent(final Intent intent) {
        boolean result = false;
        if (intent != null && intent.getId() != null
                && verifyIntentActions(intent)) {
            result = verifyIntentSubjects(intent);
        } else {
            LOG.warn("Intent ID is not specified {}", intent);
        }
        return result;
    }

    public static boolean verifyIntentActions(final Intent intent) {
        boolean result = true;
        final List<Actions> actions = intent.getActions();
        if (actions == null || actions.size() != NUM_OF_SUPPORTED_ACTION) {
            LOG.warn("Intent's action is either null or not equal to {} action {}", NUM_OF_SUPPORTED_ACTION,
                    intent);
            result = false;
        }
        return result;
    }

    public static boolean verifyIntentSubjects(final Intent intent) {
        boolean result = true;
        final List<Subjects> subjects = intent.getSubjects();
        if (subjects == null || subjects.size() != NUM_OF_SUPPORTED_EPG) {
            LOG.warn("Intent's subjects is either null or not equal to {} subjects {}", NUM_OF_SUPPORTED_EPG,
                    intent);
            result = false;
        }

        return result;
    }

    // Retrieve the end points
    public static List<String> extractEndPointGroup(final Intent intent) {
        final Uuid uuid = intent.getId();
        final List<Subjects> listSubjects = intent.getSubjects();
        final String[] endPointGroups = new String[listSubjects.size()];

        for (Subjects subjects : listSubjects) {
            final Subject subject = subjects.getSubject();
            int order = subjects.getOrder();
            verifySubjectInstance(subject, uuid);
            final EndPointGroup endPointGroup = (EndPointGroup) subject;

            final org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group
                .EndPointGroup epg = endPointGroup.getEndPointGroup();

            if (epg != null) {
                endPointGroups[order-1] = epg.getName();
            }
        }
        return Arrays.asList(endPointGroups);
    }

    public static List<EndPointGroup> extractEndPointGroups(final Intent intent)
            throws IntentElementNotFoundException {
        final Uuid intentId = intent.getId();
        final List<Subjects> subjects = intent.getSubjects();
        final EndPointGroup[] endPointGroups = new EndPointGroup[subjects
                .size()];

        for (Subjects subj : subjects) {
            try {
                final Subject subject = subj.getSubject();
                int order = subj.getOrder();
                verifySubjectInstance(subject, intentId);
                final EndPointGroup endPointGroup = (EndPointGroup) subject;
                endPointGroups[order - 1] = endPointGroup;
            } catch (IntentElementNotFoundException ie) {
                throw ie;
            }
        }
        return Arrays.asList(endPointGroups);
    }

    public static EndPointGroup extractSrcEndPointGroup(final Intent intent)
            throws IntentInvalidException {
        final EndPointGroup srcEndPointGroup;
        try {
            final List<EndPointGroup> endPointGroups = extractEndPointGroups(intent);
            srcEndPointGroup = extractEndPointGroup(endPointGroups, SRC_END_POINT_GROUP_INDEX);
        } catch (IntentElementNotFoundException | IntentInvalidException ie) {
            throw new IntentInvalidException(ie.getMessage());
        }
        return srcEndPointGroup;
    }

    public static EndPointGroup extractDstEndPointGroup(final Intent intent)
            throws IntentInvalidException {
        EndPointGroup dstEndPointGroup;
        try {
            final List<EndPointGroup> endPointGroups = extractEndPointGroups(intent);
            dstEndPointGroup = extractEndPointGroup(endPointGroups, DST_END_POINT_GROUP_INDEX);
        } catch (IntentElementNotFoundException | IntentInvalidException ie) {
            throw new IntentInvalidException(ie.getMessage());
        }
        return dstEndPointGroup;
    }

    private static EndPointGroup extractEndPointGroup(final List<EndPointGroup> endPointGroups, int targetIndex)
            throws IntentElementNotFoundException, IntentInvalidException {
        EndPointGroup endPointGroup;
        try {
            endPointGroup = endPointGroups.get(targetIndex);
        } catch (IndexOutOfBoundsException ie) {
            throw new IntentElementNotFoundException(NO_END_POINT_FOUND_MESSAGE);
        }
        if(endPointGroup == null) {
            throw new IntentInvalidException(END_POINT_NULL_MESSAGE);
        }
        return endPointGroup;
    }

    public static void verifySubjectInstance(final Subject subject, final Uuid intentId)
            throws IntentElementNotFoundException {
        if (!(subject instanceof EndPointGroup)
                && !(subject instanceof EndPointSelector)
                && !(subject instanceof EndPointGroupSelector)) {
            LOG.info("Subject is not specified: {}", intentId);
            throw new IntentElementNotFoundException(INVALID_SUBJECT_MESSAGE + intentId.getValue());
        }
    }

    public static Action getAction(final Intent intent) throws IntentInvalidException {
        final Action result;
        try {
            result = intent.getActions().get(0).getAction();
        } catch (IndexOutOfBoundsException ie) {
            throw new IntentElementNotFoundException(NO_ACTION_FOUND_MESSAGE + intent.getId());
        }

        if(result == null) {
            throw new IntentInvalidException(ACTION_NULL_MESSAGE + intent.getId());
        }
        return result;
    }
}