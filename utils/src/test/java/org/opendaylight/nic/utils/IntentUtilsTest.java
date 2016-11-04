/*
 * Copyright (c) 2015 Inocybe inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.nic.utils.exceptions.IntentElementNotFoundException;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.api.mockito.PowerMockito;

public class IntentUtilsTest {

    @Mock
    private Intent intentMock;

    @Mock
    private List<Actions> actionsMock;

    @Mock
    private Actions actions;

    @Mock
    private Action action;

    @Mock
    private List<Subjects> subjectsMock;

    @Mock
    private Subject subjectMock;

    @Mock
    private Uuid uuidMock;

    @Mock
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup epgMock;

    @Mock
    private WriteTransaction modificationMock;

    @Mock
    private InstanceIdentifier<Flow> instanceIdentifierFlowMock;

    @Mock
    private Flow flowMock;

    @Mock
    private Intent intent;

    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject subject;

    private final String DEFAULT_STR_UUID = UUID.randomUUID().toString();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(intentMock.getId()).thenReturn(uuidMock);
        Mockito.when(uuidMock.getValue()).thenReturn(DEFAULT_STR_UUID);
        Mockito.when(intentMock.getActions()).thenReturn(actionsMock);
        Mockito.when(actionsMock.size()).thenReturn(1);
        Mockito.when(intentMock.getSubjects()).thenReturn(subjectsMock);
        Mockito.when(subjectsMock.size()).thenReturn(2);
    }

    @Test
    public void testValidateIP() {
        boolean result;
        final String ip = "0.0.0.1";

        result = IntentUtils.validateIP(ip);
        Assert.assertTrue(result);
    }

    @Test
    public void testValidateEmptyIP() {
        boolean result;
        final String ip = "";

        result = IntentUtils.validateIP(ip);
        Assert.assertFalse(result);
    }

    @Test
    public void testValidateNullIP() {
        boolean result;
        final String ip = null;

        result = IntentUtils.validateIP(ip);
        Assert.assertFalse(result);
    }

    @Test
    public void testValidateIPWithLentghLessThan7() {
        boolean result;
        final String ip = "0.0.0.";

        result = IntentUtils.validateIP(ip);
        Assert.assertFalse(result);
    }

    @Test
    public void testValidateIPWithLentghMoreThan15() {
        boolean result;
        final String ip = "200.100.100.1001";

        result = IntentUtils.validateIP(ip);
        Assert.assertFalse(result);
    }

    @Test
    public void testValidateMAC() {
        boolean result;
        final String mac = "00:00:00:00:00:01";

        result = IntentUtils.validateMAC(mac);
        Assert.assertTrue(result);
    }

    @Test
    public void testValidateEmptyMAC() {
        boolean result;
        final String mac = "";

        result = IntentUtils.validateMAC(mac);
        Assert.assertFalse(result);
    }

    @Test
    public void testValidateNullMAC() {
        boolean result;
        final String mac = null;

        result = IntentUtils.validateMAC(mac);
        Assert.assertFalse(result);
    }

    @Test
    public void testVerifyIntent() {
        boolean result;

        result = IntentUtils.verifyIntent(intentMock);
        Assert.assertTrue(result);
    }

    @Test
    public void testVerifyIntentWithoutAction() {
        boolean result;

        Mockito.when(intentMock.getActions()).thenReturn(actionsMock);
        Mockito.when(actionsMock.size()).thenReturn(0);

        result = IntentUtils.verifyIntent(intentMock);
        Assert.assertFalse(result);
    }
    @Test
    public void testVerifyIntentNull() {
        boolean result;
        final Intent intentNull = null;

        result = IntentUtils.verifyIntent(intentNull);
        Assert.assertFalse(result);
    }

    @Test
    public void testVerifyIntentWithIdNull() {
        boolean result;

        Mockito.when(intentMock.getId()).thenReturn(null);
        result = IntentUtils.verifyIntent(intentMock);
        Assert.assertFalse(result);
    }

    @Test
    public void testIntentActionsSizeSupported() {
        boolean result;

        Mockito.when(intentMock.getActions()).thenReturn(null);
        result = IntentUtils.verifyIntentActions(intentMock);
        Assert.assertFalse(result);

        Mockito.when(intentMock.getActions()).thenReturn(actionsMock);
        Mockito.when(actionsMock.size()).thenReturn(0);

        result = IntentUtils.verifyIntentActions(intentMock);
        Assert.assertFalse(result);

        Mockito.when(actionsMock.size()).thenReturn(1);

        result = IntentUtils.verifyIntentActions(intentMock);
        Assert.assertTrue(result);

        Mockito.when(actionsMock.size()).thenReturn(2);

        result = IntentUtils.verifyIntentActions(intentMock);
        Assert.assertFalse(result);

        Mockito.when(actionsMock.size()).thenReturn(3);

        result = IntentUtils.verifyIntentActions(intentMock);
        Assert.assertFalse(result);
    }

    @Test
    public void testGetAction() throws IntentInvalidException {
        Action result;

        Mockito.when(actionsMock.get(0)).thenReturn(actions);
        Mockito.when(actions.getAction()).thenReturn(action);

        result = IntentUtils.getAction(intentMock);
        Assert.assertEquals(result, action);
    }

    @Test(expected = IntentElementNotFoundException.class)
    public void testGetActionWithIndexOutOfBoundsException() throws IntentInvalidException {
        Mockito.when(actionsMock.get(0))
                .thenThrow(new IndexOutOfBoundsException());

        IntentUtils.getAction(intentMock);
    }

    @Test(expected = IntentInvalidException.class)
    public void testGetActionWithIntentInvalidException() throws IntentInvalidException {
        Mockito.when(actionsMock.get(0)).thenReturn(actions);
        Mockito.when(actions.getAction()).thenReturn(null);

        IntentUtils.getAction(intentMock);
    }

    @Test
    public void testIntentSubjectsSizeSupported() {
        boolean result;

        Mockito.when(intentMock.getSubjects()).thenReturn(null);
        result = IntentUtils.verifyIntentSubjects(intentMock);
        Assert.assertFalse(result);

        Mockito.when(intentMock.getSubjects()).thenReturn(subjectsMock);

        Mockito.when(subjectsMock.size()).thenReturn(0);
        result = IntentUtils.verifyIntentSubjects(intentMock);
        Assert.assertFalse(result);

        Mockito.when(subjectsMock.size()).thenReturn(1);
        result = IntentUtils.verifyIntentSubjects(intentMock);
        Assert.assertFalse(result);

        Mockito.when(subjectsMock.size()).thenReturn(2);
        result = IntentUtils.verifyIntentSubjects(intentMock);
        Assert.assertTrue(result);

        Mockito.when(subjectsMock.size()).thenReturn(3);
        result = IntentUtils.verifyIntentSubjects(intentMock);
        Assert.assertFalse(result);

        Mockito.when(subjectsMock.size()).thenReturn(4);
        result = IntentUtils.verifyIntentSubjects(intentMock);
        Assert.assertFalse(result);
    }

    @Test
    public void testExtractEndPointGroup() {

        final String EPG_NAME_ONE = "First_EPG";

        List<String> expected = new ArrayList<String>();
        expected.add(EPG_NAME_ONE);

        Subjects subjectsMockOne = Mockito.mock(Subjects.class);

        List<Subjects> subjectMockList = new ArrayList<Subjects>();

        subjectMockList.add(subjectsMockOne);
        Mockito.when(intentMock.getSubjects()).thenReturn(subjectMockList);

        EndPointGroup subjectMockOne = Mockito.mock(EndPointGroup.class);
        Mockito.when(subjectsMockOne.getOrder()).thenReturn((short) 1);
        Mockito.when(subjectsMockOne.getSubject()).thenReturn(subjectMockOne);
        Mockito.when(subjectMockOne.getEndPointGroup()).thenReturn(epgMock);
        Mockito.when(epgMock.getName()).thenReturn(EPG_NAME_ONE);

        List<String> result = IntentUtils.extractEndPointGroup(intentMock);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void testExtractNullEndPointGroup() {
        List<String> expected = new ArrayList<String>();
        expected.add(null);

        Subjects subjectsMockOne = Mockito.mock(Subjects.class);

        List<Subjects> subjectMockList = new ArrayList<Subjects>();

        subjectMockList.add(subjectsMockOne);
        Mockito.when(intentMock.getSubjects()).thenReturn(subjectMockList);

        EndPointGroup subjectMockOne = Mockito.mock(EndPointGroup.class);
        Mockito.when(subjectsMockOne.getOrder()).thenReturn((short) 1);
        Mockito.when(subjectsMockOne.getSubject()).thenReturn(subjectMockOne);
        Mockito.when(subjectMockOne.getEndPointGroup()).thenReturn(null);

        List<String> result = IntentUtils.extractEndPointGroup(intentMock);

        Assert.assertEquals(expected, result);
    }

    @Test(expected = IntentElementNotFoundException.class)
    public void testVerifySubjectInstanceWithIntentElementNotFoundException()
            throws IntentInvalidException {
        subject = mock(
                org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject.class);

        IntentUtils.verifySubjectInstance(subject, uuidMock);
    }

    @Test
    public void testVerifySubjectInstanceWithEndPointGroup() {
        subject = mock(
                org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject.class,
                Mockito.withSettings().extraInterfaces(EndPointGroup.class));
        IntentUtils.verifySubjectInstance(subject, uuidMock);
    }

    @Test
    public void testVerifySubjectInstanceWithEndPointSelector() {
        subject = mock(
                org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject.class,
                Mockito.withSettings().extraInterfaces(EndPointSelector.class));
        IntentUtils.verifySubjectInstance(subject, uuidMock);
    }

    @Test
    public void testVerifySubjectInstanceWithEndPointGroupSelector() {
        subject = mock(
                org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject.class,
                Mockito.withSettings()
                        .extraInterfaces(EndPointGroupSelector.class));
        IntentUtils.verifySubjectInstance(subject, uuidMock);
    }

    @Test(expected = IntentElementNotFoundException.class)
    public void extractEndPointGroupWithIntentElementNotFoundException()
            throws Exception {
        PowerMockito.mockStatic(IntentUtils.class);

        EndPointGroup source = mock(EndPointGroup.class);

        List<Subjects> subjectses = spy(new ArrayList<>());
        when(intent.getSubjects()).thenReturn(subjectses);

        List<EndPointGroup> endPointGroups = spy(new ArrayList<>());
        endPointGroups.add(source);
        when(endPointGroups.get(0)).thenThrow(new IndexOutOfBoundsException());

        PowerMockito.when(IntentUtils.class, "extractEndPointGroup",
                endPointGroups, 0).thenReturn(source);

        IntentUtils.extractSrcEndPointGroup(intent);
    }

    @Test(expected = IntentInvalidException.class)
    public void extractEndPointGroupWithIntentInvalidException()
            throws Exception {
        PowerMockito.mockStatic(IntentUtils.class);

        EndPointGroup source = mock(EndPointGroup.class);

        List<Subjects> subjectses = spy(new ArrayList<>());
        when(intent.getSubjects()).thenReturn(subjectses);

        List<EndPointGroup> endPointGroups = spy(new ArrayList<>());
        endPointGroups.add(source);
        when(endPointGroups.get(0)).thenReturn(null);

        PowerMockito.when(IntentUtils.class, "extractEndPointGroup",
                endPointGroups, 0).thenReturn(source);

        IntentUtils.extractSrcEndPointGroup(intent);
    }

    @Test
    public void extractSrcEndPointGroup() throws IntentInvalidException {
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject endPointGroupSource = mock(
                org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject.class,
                Mockito.withSettings().extraInterfaces(EndPointGroup.class));

        Subjects subjectsSource = mock(Subjects.class);

        when(subjectsSource.getSubject()).thenReturn(endPointGroupSource);

        List<Subjects> subjectsList = spy(new ArrayList<>());
        subjectsList.add(subjectsSource);

        when(intent.getSubjects()).thenReturn(subjectsList);
        when(intent.getId()).thenReturn(uuidMock);

        EndPointGroup endPointGroup = IntentUtils
                .extractSrcEndPointGroup(intent);

        Assert.assertEquals(endPointGroupSource, endPointGroup);
    }

    @Test(expected = IntentInvalidException.class)
    public void extractSrcEndPointGroupWithIntentInvalidException()
            throws IntentInvalidException {
        subject = mock(
                org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject.class);

        Subjects subjects = mock(Subjects.class);
        when(subjects.getSubject()).thenReturn(subject);

        List<Subjects> subjectsList = spy(new ArrayList<>());
        subjectsList.add(subjects);

        when(intent.getSubjects()).thenReturn(subjectsList);
        when(intent.getId()).thenReturn(uuidMock);

        IntentUtils.extractSrcEndPointGroup(intent);
    }

    @Test
    public void extractDstEndPointGroup() throws IntentInvalidException {
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject endPointGroupSource = mock(
                org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject.class,
                Mockito.withSettings().extraInterfaces(EndPointGroup.class));

        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject endPointGroupDestination = mock(
                org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject.class,
                Mockito.withSettings().extraInterfaces(EndPointGroup.class));

        Subjects subjectsSource = mock(Subjects.class);
        Subjects subjectsDestination = mock(Subjects.class);
        when(subjectsSource.getSubject()).thenReturn(endPointGroupSource);
        when(subjectsDestination.getSubject())
                .thenReturn(endPointGroupDestination);

        List<Subjects> subjectsList = spy(new ArrayList<>());
        subjectsList.add(subjectsSource);
        subjectsList.add(subjectsDestination);

        when(intent.getSubjects()).thenReturn(subjectsList);
        when(intent.getId()).thenReturn(uuidMock);

        EndPointGroup endPointGroup = IntentUtils
                .extractDstEndPointGroup(intent);

        Assert.assertEquals(endPointGroupDestination, endPointGroup);
    }

    @Test(expected = IntentInvalidException.class)
    public void extractDstEndPointGroupWithIntentInvalidException()
            throws IntentInvalidException {
        subject = mock(
                org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject.class);

        Subjects subjects = mock(Subjects.class);
        when(subjects.getSubject()).thenReturn(subject);

        List<Subjects> subjectsList = spy(new ArrayList<>());
        subjectsList.add(subjects);

        when(intent.getSubjects()).thenReturn(subjectsList);
        when(intent.getId()).thenReturn(uuidMock);

        IntentUtils.extractDstEndPointGroup(intent);
    }

}
