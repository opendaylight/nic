/*
 * Copyright (c) 2016 NEC Corporation. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.vtn.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Redirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.google.common.util.concurrent.CheckedFuture;
/**
 * Unit test class for {@link VTNRenderer}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({VTNRenderer.class, VTNManagerService.class, VTNIntentParser.class, IntentUtils.class})
public class VTNRendererTest {
    /**
     * Valid Intent IDs used for testing different scenarios.
     */
    private static final String UUID_VALUE = "b9a13232-525e-4d8c-be21-cd65e3436034";
    /**
     * Collection of InstanceIdentifier and Intent.
     */
    private Map<InstanceIdentifier<?>, DataObject> dataMap;
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
    private InstanceIdentifier<?> instanceIdentifier;
    /**
     * AsyncDataChangeEvent object reference for unit testing.
     */
    private AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> asyncDataChangeEvent;
    /**
     * DataBroker object reference for unit testing.
     */
    private DataBroker dataBrokerMockObj;
    /**
     * ListenerRegistration object reference for unit testing.
     */
    private ListenerRegistration<DataChangeListener> vtnRendererListenerMockObj = null;
    /**
     * ProviderContext object reference for unit testing.
     */
    private ProviderContext providerContextMockObj;
    /**
     * VTNIntentParser object reference for unit testing.
     */
    private VTNIntentParser vtnIntentParser;
    /**
     * Allow object reference.
     */
    private Allow allow;
    /**
     * Block object reference.
     */
    private Block block;
    /**
     * Subjects object reference.
     */
    private Subjects subjects;
    /**
     * Actions object reference.
     */
    private Actions actions;
    /**
     * Uuid object reference.
     */
    private Uuid mockuuid;
    /**
     * List of Subjects.
     */
    private List<Subjects> subjectsList = null;
    /**
     * List of Actions.
     */
    private List<Actions> listActions = null;
    /**
     * EndPointGroup Source String.
     */
    private static final String EPG_SRC = "00:00:00:00:00:01";
    /**
     * EndPointGroup Destination String.
     */
    private static final String EPG_DST = "00:00:00:00:00:02";
    /**
     * Instance of WriteTransaction to perform unit testing.
     */
    private WriteTransaction transaction;
    /**
     * Instance of CheckedFuture to perform unit testing.
     */
    private CheckedFuture<Void, TransactionCommitFailedException> value = null;

    /**
     * This method creates the required objects to perform unit testing.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        allow = mock(Allow.class);
        block = mock(Block.class);
        subjects = mock(Subjects.class);
        actions = mock(Actions.class);
        dataBrokerMockObj = mock(DataBroker.class);
        vtnRendererListenerMockObj = mock(ListenerRegistration.class);
        providerContextMockObj = mock(ProviderContext.class);
        instanceIdentifier = mock(InstanceIdentifier.class);
        asyncDataChangeEvent = mock(AsyncDataChangeEvent.class);
        intentKey = mock(IntentKey.class);
        mockuuid = mock(Uuid.class);
        intent = mock(Intent.class);
        transaction = mock(WriteTransaction.class);
        value = mock(CheckedFuture.class);

        subjectsList = new ArrayList<Subjects>();
        listActions = new ArrayList<Actions>();
        dataMap = new HashMap<InstanceIdentifier<?>, DataObject>();

        subjectsList.add(subjects);
        subjectsList.add(subjects);
        listActions.add(actions);
        dataMap.put(instanceIdentifier, intent);

        when(providerContextMockObj.getSALService(DataBroker.class)).thenReturn(dataBrokerMockObj);
        when(
                dataBrokerMockObj.registerDataChangeListener(
                        eq(LogicalDatastoreType.CONFIGURATION),
                        any(InstanceIdentifier.class), any(VTNRenderer.class),
                        eq(DataChangeScope.SUBTREE))).thenReturn(
                vtnRendererListenerMockObj);

        when(asyncDataChangeEvent.getCreatedData()).thenReturn(dataMap);
        when(asyncDataChangeEvent.getUpdatedData()).thenReturn(dataMap);

        when(mockuuid.getValue()).thenReturn(UUID_VALUE);
        when(intentKey.getId()).thenReturn(mockuuid);
        when(intent.getKey()).thenReturn(intentKey);
        when(intent.getActions()).thenReturn(listActions);
        when(intent.getSubjects()).thenReturn(subjectsList);

        vtnRendererObj = PowerMockito.spy(new VTNRenderer());
    }

    /**
     * Test that checks if @{VTNRenderer#onSessionInitiated} is called
     * and then checks that Intents will be created .
     */
    @Test
    public void onSessionInitiatedTest() throws Exception {
        vtnRendererObj.onSessionInitiated(providerContextMockObj);
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
        final Set<InstanceIdentifier<?>> dataSet = new HashSet<>();
        dataSet.add(instanceIdentifier);
        dataSet.add(null);
        when(asyncDataChangeEvent.getOriginalData()).thenReturn(dataMap);
        when(asyncDataChangeEvent.getRemovedPaths()).thenReturn(dataSet);

        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        /**
         * Verifies deletion of specified data store and delete() must return true.
         */
        when(transaction.submit()).thenReturn(value);
        when(dataBrokerMockObj.newWriteOnlyTransaction()).thenReturn(transaction);

        vtnRendererObj.onSessionInitiated(providerContextMockObj);
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);

        /**
         * Verifies deletion of specified data store. Here testing invalid
         * scenario if any exception occurred in the deletion of particular data
         * store then delete() must return false only.
         */
        when(transaction.submit()).thenReturn(value);
        when(dataBrokerMockObj.newWriteOnlyTransaction()).thenReturn(transaction);
        when(value.checkedGet()).thenThrow(TransactionCommitFailedException.class);

        vtnRendererObj.onSessionInitiated(providerContextMockObj);
        vtnRendererObj.onDataChanged(asyncDataChangeEvent);

        /**
         * Verifying asyncDataChangeEvent object invoking getCreatedData, getUpdatedData, getOriginalData and
         * getRemovedPaths methods and finally checks the invocation of delete method.
         */
        verify(asyncDataChangeEvent, times(3)).getCreatedData();
        verify(asyncDataChangeEvent, times(3)).getUpdatedData();
        verify(asyncDataChangeEvent, times(3)).getOriginalData();
        verify(asyncDataChangeEvent, times(3)).getRemovedPaths();
    }

    /**
     * Test that checks if @{VTNRenderer#close} is called for
     * each close event.
     */
    @Test
    public void testClose() throws Exception {

        //Negative case - vtnRendererListener is null.
        vtnRendererObj.close();

        //Positive case - vtnRendererListener is not null.
        vtnRendererObj.onSessionInitiated(providerContextMockObj);
        vtnRendererObj.close();

    }

    /**
     * Test that checks if @{VTNRenderer#intentParser} is called
     * and verifying the Intents.
     */
    @Test
    public void testIntentParser() throws Exception {
        final List<String> listEPGs = mock(List.class);
        vtnIntentParser = mock(VTNIntentParser.class);
        PowerMockito.mockStatic(IntentUtils.class);

        //Negative case -verifyIntent
        PowerMockito.when(IntentUtils.verifyIntent(intent)).thenReturn(false);
        Whitebox.invokeMethod(vtnRendererObj, "intentParser", intent);

        //Positive case - verifyIntent
        PowerMockito.when(IntentUtils.verifyIntent(intent)).thenReturn(true);
        when(mockuuid.getValue()).thenReturn(UUID_VALUE);
        when(intent.getId()).thenReturn(mockuuid);
        PowerMockito.when(IntentUtils.extractEndPointGroup(intent)).thenReturn(listEPGs);
        listEPGs.add(EPG_DST);
        listEPGs.add(EPG_SRC);
        /**
         * Verifying vtnRenderer object invoking updateRendering method.
         */
        vtnRendererObj.onSessionInitiated(providerContextMockObj);
        when(transaction.submit()).thenReturn(value);
        when(dataBrokerMockObj.newWriteOnlyTransaction()).thenReturn(transaction);
        Whitebox.invokeMethod(vtnRendererObj, "intentParser", intent);

        verify(vtnIntentParser, times(0)).updateRendering(anyString(),
                anyString(), anyString(), anyString(),
                anyString(), Matchers.any(Intent.class));
        verify(vtnIntentParser, times(0)).rendering(anyString(),
                anyString(), anyString(), anyString(), Matchers.any(Intent.class));
        PowerMockito.verifyPrivate(vtnRendererObj, times(2)).invoke("intentParser", intent);

    }

    /**
     * Test that checks if @{VTNRenderer#getAction} is called
     * and verifying the Actions.
     */
    @Test
    public void testGetAction() throws Exception {
        String expected, actual;
        final List<Actions> listActions = mock(List.class);
        final Redirect redirect = mock(Redirect.class);

        vtnRendererObj.onDataChanged(asyncDataChangeEvent);
        /**
         * Verifying vtnRenderer object invoking rendering method
         * when getAction() returns Action object, Allow and Block object.
         */
        when(intent.getId()).thenReturn(mockuuid);
        when(actions.getAction()).thenReturn(block);
        when(listActions.get(0)).thenReturn(actions);
        when(intent.getActions()).thenReturn(listActions);
        verify(asyncDataChangeEvent).getCreatedData();
        verify(asyncDataChangeEvent).getUpdatedData();

        //Positive case - Action is block.
        actual = Whitebox.invokeMethod(vtnRendererObj, "getAction", intent);
        expected = "block";
        Assert.assertEquals("Should return  true for valid action(allow/block).", expected, actual);
        PowerMockito.verifyPrivate(vtnRendererObj, times(1)).invoke("getAction", intent);

        //Positive case - Action is allow.
        when(actions.getAction()).thenReturn(allow);
        actual = Whitebox.invokeMethod(vtnRendererObj, "getAction", intent);
        expected = "allow";
        Assert.assertEquals("Should return true for valid action(allow/block).", expected, actual);
        PowerMockito.verifyPrivate(vtnRendererObj, times(2)).invoke("getAction", intent);

        //Negative case - Invalid Action.
        when(actions.getAction()).thenReturn(redirect);
        actual = Whitebox.invokeMethod(vtnRendererObj, "getAction", intent);
        Assert.assertNull(actual);
        PowerMockito.verifyPrivate(vtnRendererObj, times(3)).invoke("getAction", intent);

        //Negative case - No action.
        when(actions.getAction()).thenReturn(null);
        actual = Whitebox.invokeMethod(vtnRendererObj, "getAction", intent);
        Assert.assertNull(actual);
        PowerMockito.verifyPrivate(vtnRendererObj, times(4)).invoke("getAction", intent);
    }
}
