/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
/**
 * JUnit test for {@link AbstractNotificationSupplierItemRoot}.
 */
public class AbstractNotificationSupplierItemRootTest {

    /**
     * Mock instance of AbstractNotificationSupplierItemRoot to perform unit testing.
     */
    private AbstractNotificationSupplierItemRoot mockItemRoot;

    @Before
    public void setup() {
        /**
         * Create required mock objects and define mocking functionality
         * for mock objects.
         */
        mockItemRoot = mock(AbstractNotificationSupplierItemRoot.class, CALLS_REAL_METHODS);
    }

    @Test
    public void onDataChangedTest() throws Exception {
        AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> mockChange = mock(AsyncDataChangeEvent.class);
        mockItemRoot.onDataChanged(mockChange);
        verify(mockItemRoot).created(mockChange.getCreatedData());
        verify(mockItemRoot).update(mockChange.getUpdatedData());
        verify(mockItemRoot).deleted(mockChange);
    }
}
