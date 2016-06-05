/*
 * Copyright © 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.utils;

import org.junit.Test;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.SubjectsKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class MappingServiceUtilsTests {

    public static final String KEY = "aaa";

    @Test
    public void testExtractSubjectDetails(){
        List<Subjects> subjectss = new ArrayList<>();
        Subjects subject = mock(Subjects.class);
        subjectss.add(subject);

        Map<String, Map<String, String>> subjects = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        map.put("ip", "10.0.0.1");

        subjects.put(KEY, map);
        Intent intent = mock(Intent.class);
        IntentMappingService intentMappingService = mock(IntentMappingService.class);
        EndPointGroup endpoint = mock(EndPointGroup.class);
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup epg =
                mock(org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup.class);

        Subject subjectsss = new EndPointGroupBuilder().setEndPointGroup(endpoint).build();

        when(subject.getKey()).thenReturn(new SubjectsKey((short)1));
        when(endpoint.getName()).thenReturn(KEY);
        when(subject.getOrder()).thenReturn((short)1);
        when(subject.getSubject()).thenReturn(epg);
        when(subject.getSubject()).thenReturn(subjectsss);
        when(intentMappingService.get(KEY)).thenReturn(map);
        when(intent.getSubjects()).thenReturn(subjectss);
        Map<String, Map<String, String>> resultSubjects =
                MappingServiceUtils.extractSubjectDetails(intent, intentMappingService);

        assertTrue(resultSubjects.size() == 1);
        assertTrue(resultSubjects.size() == subjects.size());
    }
}
