/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package nic.of.renderer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.actions.rev150122.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.actions.rev150122.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.util.concurrent.CheckedFuture;

@PrepareForTest({OFRendererDataChangeListener.class,
        BundleContext.class})
@RunWith(PowerMockRunner.class)
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
    private ReadTransaction mockReadTransaction;

    /**
     * Collection of InstanceIdentifier and Intent.
     */
    private Map<InstanceIdentifier<?>, Intent> intentMap;

    /**
     * Intent object reference for unit testing.
     */
    private Intent intent;

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
    private Subjects srcsubject;

    /**
     * Subjects object reference for destination.
     */
    private Subjects destsubject;

    /**
     * Declare list of Actions.
     */
    final List<Actions> actionlist = new ArrayList<Actions>();

    /**
     * Declare list of subjects.
     */
    private List<Subjects> subjectslist;

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
        MockitoAnnotations.initMocks(this);
        
    }

    /**
     * Test that checks if @{OFRendererDataChangeListener#onDataChanged} is
     * called for each dataChangedEvent and then checks that Intents will be
     * created for each scenarios.
     */
//    @Test
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
        verify(intentMap, times(2)).entrySet();
        verify(mockIntent, times(1)).getId();
    }

    /**
     * Test method for
     * {@link OFRendererDataChangeListener#verifyIntent(Intent)}
     * <p/>
     * verify if the intent has id as well as the number of subjects and actions.
     */
//    @Test
    public void testVerifyIntentId() {
//        PowerMockito.verifyPrivate(ofRendererDataChangeListener.invoke(
//                "verifyIntent");
//        boolean actual = Whitebox.invokeMethod(ofRendererDataChangeListener, "verifyIntent", intent);
//        assertTrue(actual);
    }

    /**
     * Test method for
     * {@link OFRendererDataChangeListener#getNodes()}
     * <p/>
     * Verify if openflow plugin returns the Node ID's
     */
    public void testGetNodes() {
        verify(mockDataBroker, times(1)).newReadOnlyTransaction();
//        verify(mockReadTransaction, times(1)).read(LogicalDatastoreType.OPERATIONAL,
//                InstanceIdentifier<Nodes>.class)
//                .checkedGet().get();
    }

    /**
     * Test method for
     * {@link OFRendererDataChangeListener#pushL2Flow(NodeId, List, Action)}
     * <p/>
     * Verify if openflow plugin pushes flows to MD-SAL
     */
    public void testPushL2Flow() {
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = mock(CheckedFuture.class);
        List<String> list = new ArrayList<String>();
        list.add(SRC_MAC);
        list.add(DST_MAC);
        NodeId id = mock(NodeId.class);
//        PowerMockito.verifyPrivate(ofRendererDataChangeListener.invoke(
//                "pushL2Flow");
//        Whitebox.invokeMethod(ofRendererDataChangeListener, "pushL2Flow", id, list, (Action) allow);
//
//        verify(mockDataBroker, times(1)).newWriteOnlyTransaction();
//        verify(mockWriteTransaction, times(1)).submit();
//        verify(commitFuture, times(1)).checkedGet();

    }
}