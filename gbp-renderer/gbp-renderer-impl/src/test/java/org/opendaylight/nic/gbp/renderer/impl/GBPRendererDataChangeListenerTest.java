/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.gbp.renderer.impl;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;

import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Unit test class for {@link GBPRendererDataChangeListener}.
 */
public class GBPRendererDataChangeListenerTest {

    /**
     * create a mock object for DataBroker class.
     */
    private DataBroker dataBrokerMockObj;

    /**
     * Collection of InstanceIdentifier and Intent.
     */
    private Map<InstanceIdentifier<?>, Intent> intentMap;

    /**
     * IntentKey object reference for unit testing.
     */
    private IntentKey intentKey;

    /**
     * Intent object reference for unit testing.
     */
    private Intent intent;

    /**
     * InstanceIdentifier object reference for unit testing.
     */
    private InstanceIdentifier<?> instanceIdentifier;

    /**
     * AsyncDataChangeEvent object reference for unit testing.
     */
    private AsyncDataChangeEvent asyncDataChangeEventMockObj;

    /**
     * GBPRendererDataChangeListener Object to perform unit testing.
     */
    private GBPRendererDataChangeListener GBPRendererObj;

    /**
     * GBPRendererDataChangeListener Object to perform unit testing.
     */
    private ListenerRegistration<DataChangeListener> listenerRegistrationMockObj;

    /**
     * This method creates the required objects to perform unit testing.
     */
    @Before
    public void setUp() {
        dataBrokerMockObj = mock(DataBroker.class);
        listenerRegistrationMockObj = mock(ListenerRegistration.class);
        when(
                dataBrokerMockObj.registerDataChangeListener(
                        eq(LogicalDatastoreType.CONFIGURATION),
                        eq(GBPRendererHelper.createIntentIid()),
                        isA(GBPRendererDataChangeListener.class),
                        eq(DataChangeScope.SUBTREE))).thenReturn(
                listenerRegistrationMockObj);
        GBPRendererObj = new GBPRendererDataChangeListener(dataBrokerMockObj);
        asyncDataChangeEventMockObj = mock(AsyncDataChangeEvent.class);
        intentMap = spy(new HashMap<InstanceIdentifier<?>, Intent>());
        when(asyncDataChangeEventMockObj.getCreatedData())
                .thenReturn(intentMap);
        when(asyncDataChangeEventMockObj.getUpdatedData())
                .thenReturn(intentMap);
        intentKey = mock(IntentKey.class);
        intent = mock(Intent.class);
        when(intent.getKey()).thenReturn(intentKey);
        instanceIdentifier = mock(InstanceIdentifier.class);
        intentMap.put(instanceIdentifier, intent);
    }

    /**
     * Test that checks if @{GBPRendererDataChangeListener#onDataChanged} is
     * called for each dataChangedEvent and then checks that Intents will be
     * created for each scenarios.
     */
    @Test
    public void testGBPRendererOnDataChanged() {

        GBPRendererObj.onDataChanged(asyncDataChangeEventMockObj);

        /**
         * Verifying asyncDataChangeEventMockObj object invoking both
         * getCreatedData and getUpdatedData methods.
         */
        verify(asyncDataChangeEventMockObj).getCreatedData();
        verify(asyncDataChangeEventMockObj).getUpdatedData();
        /**
         * Verifying the Intents map is called in create()
         */
        verify(intentMap, times(1)).entrySet();
        verify(intent, times(1)).getId();
    }

    /**
     * Test that checks if @{GBPRendererDataChangeListener#close} is called for
     * each close event.
     */
    @Test
    public void testClose() throws Exception {

        GBPRendererObj.close();
        /**
         * Verifying if close method is invoked.
         */
        verify(listenerRegistrationMockObj).close();
    }

}