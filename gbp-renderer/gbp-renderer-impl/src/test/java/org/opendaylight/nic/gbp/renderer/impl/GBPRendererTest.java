/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.gbp.renderer.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.spy;

import java.util.concurrent.Executor;

import com.google.common.util.concurrent.CheckedFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Unit test class for {@link GBPRenderer}.
 */
@PrepareForTest(FrameworkUtil.class)
@RunWith(PowerMockRunner.class)
public class GBPRendererTest {
    /**
     * Object for the class GBPRenderer.
     */
    private GBPRenderer gbpRenderer;

    /**
     * create a mock object for GBPRendererDataChangeListener class.
     */
    @Mock
    GBPRendererDataChangeListener gbpRendererDataChangeListener;

    /**
     * create a mock object for DataBroker class.
     */
    @Mock
    private DataBroker dataBroker;

    /**
     * create a mock object for the class Bundle.
     */
    @Mock
    private Bundle bundle;

    /**
     * create a mock Object for the class BundleContext.
     */
    @Mock
    private BundleContext context;

    /**
     * create a mock Object for the class ServiceRegistration.
     */
    @Mock
    ServiceRegistration<GBPRenderer> nicConsoleRegistration;

    /**
     * create a mock Object for the class ListenerRegistration.
     */
    @Mock
    ListenerRegistration<DataChangeListener> gbpRendererListener;

    /**
     * create a mock Object for the class WriteTransaction.
     */
    @Mock
    WriteTransaction writeTransactionMock;

    /**
     * create a mock Object for the class CheckedFuture.
     */
    @Mock
    CheckedFuture<Void, TransactionCommitFailedException> checkedFuture;

    @Before
    public void setUp() throws Exception {
        /**
         * Here creates objects and defines mocking functionality for mock
         * objects.
         */
        gbpRenderer = new GBPRenderer(dataBroker);
        PowerMockito.mockStatic(FrameworkUtil.class);
        when(FrameworkUtil.getBundle(GBPRenderer.class)).thenReturn(bundle);
        when(bundle.getBundleContext()).thenReturn(context);
        when(context.registerService(GBPRenderer.class, gbpRenderer, null))
                .thenReturn(nicConsoleRegistration);

        when(dataBroker.registerDataChangeListener(
                eq(LogicalDatastoreType.CONFIGURATION),
                eq(GBPRendererHelper.createIntentIid()),
                isA(GBPRendererDataChangeListener.class),
                eq(DataChangeScope.SUBTREE))).
                thenReturn(gbpRendererListener);

        when(writeTransactionMock.submit()).thenReturn(checkedFuture);
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(
                writeTransactionMock);
    }

    /**
     * Test method for {@link GBPRenderer#init()}.
     */
    @Test
    public void testinit() throws Exception {
        /**
         * It should initialize operational and default config data in MD-SAL
         * data store.
         */
        gbpRenderer.init();

        /**
         * Verifying context object invoking registerService method.
         */
        verify(context).registerService(GBPRenderer.class, gbpRenderer, null);
        PowerMockito.verifyStatic(times(1));
    }

    /**
     * Test method for {@link GBPRenderer#Close()}.
     */
    @Test
    public void testClose() throws Exception {
        /**
         * Valid scenario - it should initialize operational and default config
         * data in MD-SAL data store and should close listeners properly.
         * Verifying nicConsoleRegistration object invoking unregister method.
         * Verifying gbpRendererListener object invoking close method.
         */
        gbpRenderer.init();
        gbpRenderer.close();
        verify(nicConsoleRegistration).unregister();
        verify(gbpRendererListener).close();

        /**
         * Invalid scenario - it should not delete any node because of invalid
         * arguments passed as arguments.
         */
        GBPRenderer gbpRendererCheck = spy(new GBPRenderer(null));
        PowerMockito.mockStatic(GBPRendererHelper.class);
        gbpRendererCheck.close();
        PowerMockito.verifyStatic(times(0));
    }

    /**
     * Test method for {@link GBPRenderer#deleteNode()}.
     */
    @Test
    public void testDeleteNode() throws Exception {
        /**
         * Valid scenario - it should retrieve proper transaction and delete
         * nodes from that transaction.
         */
        gbpRenderer.close();
        /**
         * Verifying writeTransactionMock object invoking delete method.
         * Verifying writeTransactionMock object invoking submit method.
         * Verifying checkedFuture object invoking addListner method.
         */
        verify(writeTransactionMock).delete(any(LogicalDatastoreType.class),
                any(InstanceIdentifier.class));
        verify(writeTransactionMock).submit();
        verify(checkedFuture).addListener(any(Runnable.class),
                any(Executor.class));
    }
}
