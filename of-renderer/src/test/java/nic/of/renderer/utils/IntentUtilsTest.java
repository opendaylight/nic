package nic.of.renderer.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IntentUtilsTest {

    @Mock
    private Intent intentMock;

    @Mock
    private List<Actions> actionsMock;

    @Mock
    private List<Subjects> subjectsMock;

    @Mock
    private Subject subjectMock;

    @Mock
    private Uuid uuidMock;

    @Mock
    private org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group
    .EndPointGroup epgMock;

    @Mock
    private WriteTransaction modificationMock;

    @Mock
    private InstanceIdentifier<Flow> instanceIdentifierFlowMock;

    @Mock
    private Flow flowMock;

    private final String DEFAULT_STR_UUID = UUID.randomUUID().toString();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(intentMock.getId()).thenReturn(uuidMock);
        Mockito.when(uuidMock.getValue()).thenReturn(DEFAULT_STR_UUID);
    }

    @Test
    public void testVerifyIntent() {
        boolean result;
        final Intent intentNull = null;

        result = IntentUtils.verifyIntent(intentNull);
        Assert.assertFalse(result);

        result = IntentUtils.verifyIntent(intentMock);
        Assert.assertTrue(result);
    }

    @Test
    public void testIntentActionsSizeSupported() {
        boolean result;

        Mockito.when(intentMock.getActions()).thenReturn(actionsMock);
        Mockito.when(actionsMock.size()).thenReturn(0);

        result = IntentUtils.verifyIntentActions(intentMock);
        Assert.assertTrue(result);

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
    public void testIntentSubjectsSizeSupported() {
        boolean result;

        Mockito.when(intentMock.getSubjects()).thenReturn(subjectsMock);

        Mockito.when(subjectsMock.size()).thenReturn(0);
        result = IntentUtils.verifyIntentSubjects(intentMock);
        Assert.assertTrue(result);

        Mockito.when(subjectsMock.size()).thenReturn(1);
        result = IntentUtils.verifyIntentSubjects(intentMock);
        Assert.assertTrue(result);

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
        Mockito.when(subjectsMockOne.getSubject()).thenReturn(subjectMockOne);
        Mockito.when(subjectMockOne.getEndPointGroup()).thenReturn(epgMock);
        Mockito.when(epgMock.getName()).thenReturn(EPG_NAME_ONE);

        List<String> result  = IntentUtils.extractEndPointGroup(intentMock);

        Assert.assertEquals(expected, result);
    }
}
