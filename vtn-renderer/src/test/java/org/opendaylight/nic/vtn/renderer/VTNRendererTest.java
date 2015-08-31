/**
 * Copyright (c) 2015 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Matchers;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Unit test class for {@link VTNRenderer}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(VTNRenderer.class)
public class VTNRendererTest {
    /**
     * Valid Intent IDs used for testing different scenarios.
     */
    private static final String UUID_VALUE = "b9a13232-525e-4d8c-be21-cd65e3436034";
    /**
     * Valid IP address used for testing different scenarios.
     */
    private static final String IP_ADDRESS = "10.0.0.1";
    /**
     * Collection of InstanceIdentifier and Intent.
     */
    private Map<InstanceIdentifier, Intent> dataMap;
    /**
     * IntentKey object reference for unit testing.
     */
    private IntentKey intentKey;
    /**
     * Intent object reference for unit testing.
     */
    private Intent intent;
    /**
     * VTNRenderer object reference to perform unit testing.
     */
    private VTNRenderer vtnRendererObj;
    /**
     * InstanceIdentifier object reference for unit testing.
     */
    private InstanceIdentifier instanceIdentifier;
    /**
     * AsyncDataChangeEvent object reference for unit testing.
     */
    private AsyncDataChangeEvent asyncDataChangeEvent;
    /**
     * VTNIntentParser object reference for unit testing.
     */
    private VTNIntentParser mockVTNIntentParser;
    /**
     * DataBroker object reference for unit testing.
     */
    private DataBroker dataBrokerMockObj;
    /**
     * ListenerRegistration object reference for unit testing.
     */
    private ListenerRegistration<DataChangeListener> vtnRendererListenerMockObj = null;

    /**
     * This method creates the required objects to perform unit testing.
     */
    @Before
    public void setUp() throws Exception {
        dataBrokerMockObj = mock(DataBroker.class);
        vtnRendererListenerMockObj = mock(ListenerRegistration.class);
        when(dataBrokerMockObj.registerDataChangeListener(
                        eq(LogicalDatastoreType.CONFIGURATION),
                        eq(InstanceIdentifier.builder(Intents.class)
                            .build()),
                        isA(VTNRenderer.class),
                        eq(DataChangeScope.SUBTREE))).thenReturn(
                vtnRendererListenerMockObj);
        vtnRendererObj = PowerMockito.spy(new VTNRenderer(dataBrokerMockObj));
        mockVTNIntentParser = PowerMockito.spy(new VTNIntentParser(dataBrokerMockObj));
        Whitebox.setInternalState(vtnRendererObj, "vtnIntentParser", mockVTNIntentParser);
        asyncDataChangeEvent = mock(AsyncDataChangeEvent.class);
        dataMap = new HashMap<InstanceIdentifier, Intent>();
        when(asyncDataChangeEvent.getCreatedData()).thenReturn(dataMap);
        when(asyncDataChangeEvent.getUpdatedData()).thenReturn(dataMap);
        intentKey = mock(IntentKey.class);
        Uuid mockuuid = mock(Uuid.class);
        when(mockuuid.getValue()).thenReturn(UUID_VALUE);
        when(intentKey.getId()).thenReturn(mockuuid);
        intent = mock(Intent.class);
        when(intent.getKey()).thenReturn(intentKey);
        instanceIdentifier = mock(InstanceIdentifier.class);
        dataMap.put(instanceIdentifier, intent);
    }

    /**
     * Test that checks if @{VTNRenderer#onDataChanged} is called
     * and then checks that Intents will be created .
     */
    @Test
    public void testOnDataChangedForCreated() throws Exception {
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        /**
         * Verifying asyncDataChangeEvent object invoking getCreatedData method.
         */
        verify(intent, times(4)).getId();
        verify(asyncDataChangeEvent).getCreatedData();
    }

    /**
     * Test that checks if @{VTNRenderer#onDataChanged} is called
     * and then checks that Intents will be updated.
     */
    @Test
    public void testOnDataChangedForUpdated() throws Exception {
        dataMap.put(null, null);
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        /**
         * Verifying asyncDataChangeEvent object invoking both getCreatedData and getUpdatedData methods.
         */
        verify(asyncDataChangeEvent).getCreatedData();
        verify(asyncDataChangeEvent).getUpdatedData();
    }

    /**
     * Test that checks if @{VTNRenderer#onDataChanged} is called
     * and then checks that Intents will be deleted.
     */
    @Test
    public void testOnDataChangedForDeleted() throws Exception {
        final Set<InstanceIdentifier> dataSet = new HashSet<InstanceIdentifier>();
        dataSet.add(instanceIdentifier);
        dataSet.add(null);
        when(asyncDataChangeEvent.getOriginalData()).thenReturn(dataMap);
        when(asyncDataChangeEvent.getRemovedPaths()).thenReturn(dataSet);
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        /**
         * Verifying asyncDataChangeEvent object invoking getCreatedData, getUpdatedData, getOriginalData and
         * getRemovedPaths methods and finally checks the invocation of delete method.
         */
        verify(asyncDataChangeEvent).getCreatedData();
        verify(asyncDataChangeEvent).getUpdatedData();
        verify(asyncDataChangeEvent).getOriginalData();
        verify(asyncDataChangeEvent).getRemovedPaths();
        verify(mockVTNIntentParser).delete(anyString());
    }

    /**
     * Test that checks if @{VTNRenderer#onDataChanged} is called
     * and then checks the behaviour of Subjects.
     */
    @Test
    public void testIntentParserForSubjects() throws Exception {
        final Uuid uuid = mock(Uuid.class);
        final Subjects subjects = mock(Subjects.class);
        final List<Subjects> subjectsList = new ArrayList<Subjects>();
        when(uuid.getValue()).thenReturn(UUID_VALUE);
        when(intent.getId()).thenReturn(uuid);
        subjectsList.add(subjects);
        when(intent.getSubjects()).thenReturn(null,subjectsList);
        /**
         * Verifying Intent object invoking getSubjects method when getSubjects return null and
         * verifying Subjects object invoking getSubject method.
         */
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        verify(intent, times(2)).getSubjects();
        verify(subjects, times(0)).getSubject();
        /**
         * Verifying Intent object invoking getSubjects method when getSubjects return list
         * contains single subject object and verifying Subjects object invoking getSubject method.
         */
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        verify(intent, times(4)).getSubjects();
        verify(subjects, times(0)).getSubject();
        /**
         * Verifying Intent object invoking getSubjects method when getSubjects return list
         * contains two subject objects and verifying Subjects object invoking getSubject method.
         */
        subjectsList.add(subjects);
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        verify(intent, times(6)).getSubjects();
        verify(subjects, times(2)).getSubject();
    }

    /**
     * Test that checks if @{VTNRenderer#onDataChanged} is called
     * and then checks the behaviour of endpoints.
     */
    @Test
    public void testIntentParserForEndPointGroups() throws Exception {
        final List<Subjects> subjectsList = new ArrayList<Subjects>();
        final Uuid  uuid  = mock(Uuid.class);
        final EndPointGroup endPointGroup = mock(EndPointGroup.class);
        when(uuid.getValue()).thenReturn(UUID_VALUE);
        when(intent.getId()).thenReturn(uuid);
        Subjects subjects = mock(Subjects.class);
        subjectsList.add(subjects);
        subjectsList.add(subjects);
        when(endPointGroup.getEndPointGroup()).thenReturn(null,
                mock(org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.
                intent.subjects.subject.end.point.group.EndPointGroup.class));
        when(subjects.getSubject()).thenReturn(null, mock(Subject.class), endPointGroup);
        when(intent.getSubjects()).thenReturn(subjectsList);
        /**
         * Verifying endPointGroup object not invoking getEndPointGroup when getSubject() return null.
         */
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        verify(endPointGroup, times(0)).getEndPointGroup();
        /**
         * Verifying endPointGroup object invoking getEndPointGroup when getSubject() return Subject object.
         */
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        verify(endPointGroup, times(3)).getEndPointGroup();
        /**
         * Verifying endPointGroup object invoking getEndPointGroup when getSubject() return EndPointGroup object.
         */
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        verify(endPointGroup, times(7)).getEndPointGroup();
    }

    /**
     * Test that checks if @{VTNRenderer#onDataChanged} is called
     * and then checks the behaviour of actions.
     */
    @Test
    public void testIntentParserForActions() throws Exception {
        final Uuid  uuid  = mock(Uuid.class);
        final EndPointGroup endPointGroup = mock(EndPointGroup.class);
        final Subjects subjects = mock(Subjects.class);
        final List<Subjects> subjectsList = new ArrayList<Subjects>();
        final List<Actions> emptyList = new ArrayList<Actions>();
        final List<Actions> listActions = new ArrayList<Actions>();
        final Actions actions = mock(Actions.class);
        when(uuid.getValue()).thenReturn(UUID_VALUE);
        when(intent.getId()).thenReturn(uuid);
        subjectsList.add(subjects);
        subjectsList.add(subjects);
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.
                end.point.group.EndPointGroup epg = mock(org.opendaylight.yang.gen.v1.urn.opendaylight
                .intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup.class);
        when(epg.getName()).thenReturn(IP_ADDRESS);
        when(endPointGroup.getEndPointGroup()).thenReturn(epg);
        when(subjects.getSubject()).thenReturn(endPointGroup);
        when(intent.getSubjects()).thenReturn(subjectsList);
        when(actions.getAction()).thenReturn(mock(Allow.class), mock(Block.class), null);
        listActions.add(actions);
        when(intent.getActions()).thenReturn(null, emptyList, listActions);
        /**
         * Verifying vtnRenderer object invoking rendering and updateRendering methods
         * when getActions() returns null, empty list and list contains actions object.
         */
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        PowerMockito.doReturn(true).when(vtnRendererObj, "hasRendered", anyString());
        verify(mockVTNIntentParser, times(0)).updateRendering(anyString(),
                anyString(), anyString(), anyString(),
                anyString(), Matchers.any(Intent.class));
        verify(mockVTNIntentParser, times(2)).rendering(anyString(),
                anyString(), anyString(), anyString(), Matchers.any(Intent.class));
        /**
         * Verifying vtnRenderer object invoking rendering method
         * when getAction() returns null, Allow and Block object.
         */
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        verify(mockVTNIntentParser, times(2)).rendering(anyString(),
                anyString(), anyString(), anyString(), Matchers.any(Intent.class));
    }

    /**
     * Test that checks if @{VTNRenderer#onDataChanged} is called
     * and then checks the behaviour of rendering.
     */
    @Test
    public void testIntentParserForRendering() throws Exception {
        final Uuid  uuid  = mock(Uuid.class);
        final EndPointGroup endPointGroup = mock(EndPointGroup.class);
        final Subjects subjects = mock(Subjects.class);
        final List<Subjects> subjectsList = new ArrayList<Subjects>();
        final List<Actions> emptyList = new ArrayList<Actions>();
        final List<Actions> listActions = new ArrayList<Actions>();
        final Actions actions = mock(Actions.class);
        when(uuid.getValue()).thenReturn(UUID_VALUE);
        when(intent.getId()).thenReturn(uuid);
        subjectsList.add(subjects);
        subjectsList.add(subjects);
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.
                end.point.group.EndPointGroup epg = mock(org.opendaylight.yang.gen.v1.urn.opendaylight
                .intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup.class);
        when(epg.getName()).thenReturn(IP_ADDRESS);
        when(endPointGroup.getEndPointGroup()).thenReturn(epg);
        when(subjects.getSubject()).thenReturn(endPointGroup);
        when(intent.getSubjects()).thenReturn(subjectsList);
        when(actions.getAction()).thenReturn(mock(Allow.class), mock(Block.class), null);
        listActions.add(actions);
        when(intent.getActions()).thenReturn(null, emptyList, listActions);
        PowerMockito.doReturn(true).when(vtnRendererObj, "hasRendered", anyString());
        /**
         * Verifying vtnRenderer object invoking updateRendering method
         * when getActions() returns null, empty list and list contains actions object.
         */
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        verify(mockVTNIntentParser, times(2)).updateRendering(anyString(),
                anyString(), anyString(), anyString(),
                anyString(), Matchers.any(Intent.class));
    }

    /**
     * Test that checks if @{VTNRenderer#Close} is called for
     * each close event.
     */
    @Test
    public void testClose() throws Exception {
        vtnRendererObj.close();
        /**
         * Verifying if close method is invoked.
         */
        verify(vtnRendererListenerMockObj).close();
    }
}
