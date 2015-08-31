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

import nic.of.renderer.OFRenderer;
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
 * Unit test class for {@link nic.of.renderer.OFRenderer}.
 */
@PrepareForTest({FrameworkUtil.class})
@RunWith(PowerMockRunner.class)
public class OFRendererTest {
    /**
     * Object for the class GBPRenderer.
     */
    private OFRenderer ofRenderer;

    /**
     * create a mock object for GBPRendererDataChangeListener class.
     */
    @Mock
    OFRendererDataChangeListener ofRendererDataChangeListener;

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
    ServiceRegistration<OFRenderer> nicConsoleRegistration;

    /**
     * create a mock Object for the class ListenerRegistration.
     */
    @Mock
    ListenerRegistration<DataChangeListener> ofRendererListener;

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
        ofRenderer = new OFRenderer(dataBroker);
        PowerMockito.mockStatic(FrameworkUtil.class);
        when(FrameworkUtil.getBundle(OFRenderer.class)).thenReturn(bundle);
        when(bundle.getBundleContext()).thenReturn(context);
        when(context.registerService(OFRenderer.class, ofRenderer, null))
                .thenReturn(nicConsoleRegistration);

        when(dataBroker.registerDataChangeListener(
                eq(LogicalDatastoreType.CONFIGURATION),
                eq(InstanceIdentifier.builder(Intents.class)
                        .child(Intent.class)
                        .build()),
                isA(OFRendererDataChangeListener.class),
                eq(DataChangeScope.SUBTREE))).
                thenReturn(ofRendererListener);

        when(writeTransactionMock.submit()).thenReturn(checkedFuture);
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(
                writeTransactionMock);
    }

    /**
     * Test method for {@link OFRenderer#init()}.
     */
    @Test
    public void testinit() throws Exception {
        /**
         * It should initialize operational and default config data in MD-SAL
         * data store.
         */
        ofRenderer.init();

        /**
         * Verifying context object invoking registerService method.
         */
        verify(context).registerService(OFRenderer.class, ofRenderer, null);
        PowerMockito.verifyStatic(times(1));
    }
}
