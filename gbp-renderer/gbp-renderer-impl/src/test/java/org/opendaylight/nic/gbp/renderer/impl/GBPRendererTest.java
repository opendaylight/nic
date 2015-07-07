/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.gbp.renderer.impl;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Matchers.any;

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
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.util.concurrent.CheckedFuture;

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
     * Object for the class GBPRendererDataChangeListener.
     */
    private GBPRendererDataChangeListener gbpRendererDataChangeListener;

    /**
     * create a mock object for DataBroker class.
     */
    @Mock private DataBroker dataBroker;

    /**
     * create a mock object for the class bundle.
     */
    @Mock private Bundle bundle;

    /**
     * create a mock Object for the class BundleContext.
     */
    @Mock private BundleContext context;

    @Before
    public void setUp() throws Exception {

        gbpRenderer = new GBPRenderer(dataBroker);
        gbpRendererDataChangeListener = new GBPRendererDataChangeListener(dataBroker);
        PowerMockito.mockStatic(FrameworkUtil.class);
        when(FrameworkUtil.getBundle(GBPRenderer.class)).thenReturn(bundle);
        when(bundle.getBundleContext()).thenReturn(context);
        gbpRenderer.init();
    }

    /**
     * Test method for {@link GBPRenderer#Close()}.
     */
    @Test
    public void testDeleteNode() throws Exception {
        ServiceRegistration<GBPRenderer> nicConsoleRegistration = mock(ServiceRegistration.class);
        when(context.registerService(GBPRenderer.class, gbpRenderer, null)).thenReturn(nicConsoleRegistration);
        WriteTransaction writeTransactionMock = mock(WriteTransaction.class);
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransactionMock);
        doNothing().when(writeTransactionMock).delete(any(LogicalDatastoreType.class), any(InstanceIdentifier.class));
        CheckedFuture<Void,TransactionCommitFailedException> checkedFuture = mock(CheckedFuture.class);
        when(writeTransactionMock.submit()).thenReturn(checkedFuture);

        /**
         * Success case - passing valid data.
         *
         * Verifying nicConsoleRegistration object invoking unregister method.
         * Verifying writeTransactionMock object invoking delete method.
         * Verifying writeTransactionMock object invoking submit method.
         */
        gbpRenderer.init();
        gbpRenderer.close();
        verify(nicConsoleRegistration, times(1)).unregister();
        verify(writeTransactionMock, times(1)).delete(any(LogicalDatastoreType.class), any(InstanceIdentifier.class));
        verify(writeTransactionMock, times(1)).submit();

        /**
         * Failure case - passing invalid data.
         */
        GBPRenderer gbpRendererCheck = new GBPRenderer(null);
        gbpRendererCheck.close();
    }
}
