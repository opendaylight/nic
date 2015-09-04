/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package nic.of.renderer;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.ActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.SubjectsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.AllowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.BlockBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit test class for {@link nic.of.renderer.OFRendererDataChangeListener}.
 */
public class OFRendererDataChangeListenerTest {

    /**
     * create a mock object for DataBroker class.
     */
    @Mock
    private DataBroker mockDataBroker;

    /**
     * IntentKey object reference for unit testing.
     */
    @Mock
    private IntentKey mockIntentKey;

    /**
     * Mock intent object reference for unit testing.
     */
    @Mock
    private Intent mockIntent;

    /**
     * InstanceIdentifier object reference for unit testing.
     */
    @Mock
    private InstanceIdentifier<?> mockInstanceIdentifier;

    /**
     * AsyncDataChangeEvent object reference for unit testing.
     */
    @Mock
    private AsyncDataChangeEvent mockAsyncDataChangeEvent;

    /**
     * OFRendererDataChangeListener Object to perform unit testing.
     */
    @Mock
    private ListenerRegistration<DataChangeListener> mockListenerRegistration;

    /**
     * Mock instance of WriteTransaction to perform unit testing.
     */
    @Mock
    private WriteTransaction mockWriteTransaction;

    /**
     * Mock instance of ReadTransaction to perform unit testing.
     */
    @Mock
    private ReadOnlyTransaction mockReadTransaction;

    /**
     * Collection of InstanceIdentifier and Intent.
     */
    private Map<InstanceIdentifier<?>, Intent> intentMap;

    /**
     * Intent object reference for unit testing.
     */
    private Intent intent;

    /**
     * Intent object reference for unit testing.
     */
    private Intent falseIntent;

    /**
     * OFRendererDataChangeListener Object to perform unit testing.
     */
    private OFRendererDataChangeListener ofRendererDataChangeListener;

    /**
     * IntentKey object reference.
     */
    private IntentKey intentKey;

    /**
     * Allow object reference.
     */
    private Allow allow;

    /**
     * Block object reference.
     */
    private Block block;

    /**
     * Actions object reference for Allow action.
     */
    private Actions actionAllow;

    /**
     * Actions object reference for Block action.
     */
    private Actions actionBlock;

    /**
     * Subjects object reference for source.
     */
    private Subjects srcSubject;

    /**
     * Subjects object reference for destination.
     */
    private Subjects destSubject;

    /**
     * Declare list of Actions.
     */
    final List<Actions> actionList = new ArrayList<Actions>();

    /**
     * Declare list of subjects.
     */
    private List<Subjects> subjectsList;

    /**
     * String declaration for UniqueId.
     */
    private static final String UNIQUE_ID = "891fc7a8-cca7-45ee-9128-3294b96307d0";

    /**
     * String declaration for souce.
     */
    private static final String SRC_MAC = "00:00:00:00:00:01";

    /**
     * String declaration for destination.
     */
    private static final String DST_MAC = "00:00:00:00:00:02";

    /**
     * This method creates the required objects to perform unit testing.
     */
    @Before
    public void setUp() {
        initMocks(this);
        when(
                mockDataBroker.registerDataChangeListener(
                        eq(LogicalDatastoreType.CONFIGURATION),
                        eq(InstanceIdentifier.builder(Intents.class)
                                .child(Intent.class)
                                .build()),
                        isA(OFRendererDataChangeListener.class),
                        eq(DataChangeScope.SUBTREE))).thenReturn(
                mockListenerRegistration);
        ofRendererDataChangeListener = spy(new OFRendererDataChangeListener(mockDataBroker));
        intentMap = spy(new HashMap<InstanceIdentifier<?>, Intent>());
        when(mockAsyncDataChangeEvent.getCreatedData())
                .thenReturn(intentMap);
        when(mockIntent.getKey()).thenReturn(mockIntentKey);
        intentMap.put(mockInstanceIdentifier, mockIntent);

        intentKey = new IntentKey(new Uuid(UNIQUE_ID));
        allow = new AllowBuilder().build();
        block = new BlockBuilder().build();
        actionAllow = new ActionsBuilder().setAction(allow).build();
        actionBlock = new ActionsBuilder().setAction(block).build();
        actionList.add(actionAllow);
        EndPointGroup src = new EndPointGroupBuilder().setName(UNIQUE_ID)
                .build();
        EndPointGroup dest = new EndPointGroupBuilder().setName(UNIQUE_ID)
                .build();
        Subject srcAddress = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder()
                .setEndPointGroup(src).build();
        Subject destAddress = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder()
                .setEndPointGroup(dest).build();
        srcSubject = new SubjectsBuilder().setSubject(srcAddress).build();
        destSubject = new SubjectsBuilder().setSubject(destAddress).build();
        subjectsList = new ArrayList<Subjects>();
        subjectsList.add(srcSubject);
        subjectsList.add(destSubject);
        intent = new IntentBuilder().setKey(intentKey).
                setActions(actionList).setSubjects(subjectsList).build();
        falseIntent = new IntentBuilder().setKey(intentKey).
                setActions(actionList).setSubjects(null).build();
    }

    /**
     * Test that checks if @{OFRendererDataChangeListener#onDataChanged} is
     * called for each dataChangedEvent and then checks that Intents will be
     * created for each scenarios.
     */
    @Test
    public void testOFRendererOnDataChanged() {

        ofRendererDataChangeListener.onDataChanged(mockAsyncDataChangeEvent);

       /**
         * Verifying mockAsyncDataChangeEvent object invoking
         * getCreatedData.
         */
        verify(mockAsyncDataChangeEvent).getCreatedData();
        /**
         * Verifying the Intents map is called in create()
         */
        verify(intentMap, times(1)).entrySet();
        verify(mockIntent, times(1)).getId();
    }

    /**
     * Positiver test method for
     * {@link OFRendererDataChangeListener#verifyIntent(Intent)}
     * <p/>
     * verify if the intent has id as well as the number of subjects and actions.
     */
    @Test
    public void testVerifyTrueIntent() {
        try {
            boolean actual = Whitebox.invokeMethod(ofRendererDataChangeListener, "verifyIntent", intent);
            assertTrue(actual);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Negative test method for
     * {@link OFRendererDataChangeListener#verifyIntent(Intent)}
     * <p/>
     * verify intent should fail with malformed Intent.
     */
    @Test
    public void testVerifyFalseIntent() {
        try {
            boolean actual = Whitebox.invokeMethod(ofRendererDataChangeListener, "verifyIntent", falseIntent);
            assertFalse(actual);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test method for
     * {@link OFRendererDataChangeListener#getNodes()}
     * <p/>
     * Verify if openflow plugin returns the Node ID's
     */
    @Test
    public void testGetNodes() {
        try {
            CheckedFuture<Optional<Nodes>, ReadFailedException> commitFuture = mock(CheckedFuture.class);
            Optional<Nodes> mockNodesOptional= mock(Optional.class);
            Nodes nodeList = mock(Nodes.class);
            when(mockDataBroker.newReadOnlyTransaction()).thenReturn(mockReadTransaction);
            when(mockReadTransaction.read(LogicalDatastoreType.OPERATIONAL,
                    InstanceIdentifier.create(Nodes.class))).thenReturn(commitFuture);
            when(commitFuture.checkedGet()).thenReturn(mockNodesOptional);
            when(mockNodesOptional.get()).thenReturn(nodeList);
            Whitebox.invokeMethod(ofRendererDataChangeListener, "getNodes");
            //Verify that the MD-SAL is read for nodes
            verify(mockDataBroker).newReadOnlyTransaction();
            verify(mockReadTransaction).read(LogicalDatastoreType.OPERATIONAL,
                    InstanceIdentifier.create(Nodes.class));
            verify(commitFuture).checkedGet();
            verify(mockNodesOptional).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test method for
     * {@link OFRendererDataChangeListener#pushL2Flow(NodeId, List, Action)}}
     * <p/>
     * Verify if openflow plugin pushes flows to MD-SAL
     */
    @Test
    public void testPushFlow() {
        try {
            CheckedFuture<Void, TransactionCommitFailedException> commitFuture = mock(CheckedFuture.class);
            when(mockDataBroker.newWriteOnlyTransaction()).thenReturn(mockWriteTransaction);
            when(mockWriteTransaction.submit()).thenReturn(commitFuture);
            when(commitFuture.checkedGet()).thenReturn(null);
            doReturn(null).when(commitFuture).checkedGet();
            List<String> list = new ArrayList<String>();
            list.add(SRC_MAC);
            list.add(DST_MAC);
            NodeId id = mock(NodeId.class);
            //Test for allow
            Whitebox.invokeMethod(ofRendererDataChangeListener, "pushL2Flow", id, list, allow);
            //Test for block
            Whitebox.invokeMethod(ofRendererDataChangeListener, "pushL2Flow", id, list, block);
            //Verify that flow is written to MD-SAL
            verify(mockDataBroker, times(2)).newWriteOnlyTransaction();
            verify(mockWriteTransaction, times(2)).submit();
            verify(commitFuture, times(2)).checkedGet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}