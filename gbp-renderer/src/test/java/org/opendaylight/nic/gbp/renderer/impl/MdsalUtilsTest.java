/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.gbp.renderer.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.base.Optional;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * JUnit test for {@link MdsalUtils}.
 */
 public class MdsalUtilsTest {
    /**
     * Instance of DataBroker to perform unit testing.
     */
    private DataBroker dataBroker;

    /**
     * Instance of CheckedFuture to perform unit testing.
     */
    private CheckedFuture checkedFuture;

    /**
     * Instance of WriteTransaction to perform unit testing.
     */
    private WriteTransaction writeTransaction;

    /**
     * Instance of ReadOnlyTransaction to perform unit testing.
     */
    private ReadOnlyTransaction readOnlyTransaction;

    /**
     * Instance of MdsalUtils to perform unit testing.
     */
    private MdsalUtils mdsalUtils;

    /**
     * Instance of Optional to perform unit testing.
     */
    private Optional optional;

    /**
     * Instance of InstanceIdentifier to perform unit testing.
     */
    private InstanceIdentifier instanceIdentifier;

    /**
     * It creates the required objects for every unit test cases.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception{
        instanceIdentifier = mock(InstanceIdentifier.class);
        checkedFuture = mock(CheckedFuture.class);
        optional = mock(Optional.class);
        writeTransaction = mock(WriteTransaction.class);
        readOnlyTransaction = mock(ReadOnlyTransaction.class);
        dataBroker = mock(DataBroker.class);

        when(checkedFuture.checkedGet()).thenReturn(optional);
        when(writeTransaction.submit()).thenReturn(checkedFuture);
        when(readOnlyTransaction.read(isA(LogicalDatastoreType.class), isA(InstanceIdentifier.class))).
                thenReturn(checkedFuture);
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransaction);
        when(dataBroker.newReadOnlyTransaction()).thenReturn(readOnlyTransaction);

        mdsalUtils = new MdsalUtils(dataBroker);
    }

    /**
     * Test case for {@link MdsalUtils#delete()}
     */
    @Test
    public void testDelete()throws Exception {
        /**
         * Verifies deletion of specified data store by passing valid store and path then delete() must return true
         * if transaction successfully deleted particular data store in that particulate path.
         */
        assertTrue(mdsalUtils.delete(LogicalDatastoreType.CONFIGURATION, instanceIdentifier));
        verify(writeTransaction).delete(LogicalDatastoreType.CONFIGURATION, instanceIdentifier);
        verify(writeTransaction).submit();
        /**
         * Verifies deletion of specified data store. Here testing invalid scenario if any exception occurred
         * in the deletion of particular data store then delete() must return false only.
         */
        when(checkedFuture.checkedGet()).thenThrow(TransactionCommitFailedException.class);
        assertFalse(mdsalUtils.delete(LogicalDatastoreType.CONFIGURATION, instanceIdentifier));
        verify(writeTransaction, times(2)).delete(LogicalDatastoreType.CONFIGURATION, instanceIdentifier);
        verify(writeTransaction, times(2)).submit();
    }

    /**
     * Test case for {@link MdsalUtils#Merge()}
     */
    @Test
    public void testMerge()throws Exception {
        final DataObject dataObject = mock(DataObject.class);
        /**
         * Verifies that merge of specified data store. Here testing valid scenario by passing valid store and path
         * then merge() returns true if transaction successfully merge particular data store in that particulate path.
         */
        assertTrue(mdsalUtils.merge(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, dataObject));
        verify(writeTransaction).merge(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, dataObject, true);
        verify(writeTransaction).submit();
        /**
         * Verifies deletion of specified data store. Here testing invalid scenario if any exception occurred in the
         * merging of particular data store then merge() must return false only.
         */
        when(checkedFuture.checkedGet()).thenThrow(TransactionCommitFailedException.class);
        assertFalse(mdsalUtils.merge(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, dataObject));
        verify(writeTransaction, times(2)).merge(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, dataObject, true);
        verify(writeTransaction, times(2)).submit();
    }

    /**
     * Test case for {@link MdsalUtils#put()}
     */
    @Test
    public void testPut() throws Exception {
        final DataObject dataObject = mock(DataObject.class);
        /**
         * Verifies creation of specified data store or updation if it already available by passing valid store and
         * path then put() returns true if transaction is created or updated data store in that particulate path.
         */
        assertTrue(mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, dataObject));
        verify(writeTransaction).put(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, dataObject, true);
        verify(writeTransaction).submit();
        /**
         * Verifies creation of specified data store or updation if it already available.Here testing invalid scenario
         * if any exception occurred in creation or updation of particular data store then put() returns false only.
         */
        when(checkedFuture.checkedGet()).thenThrow(TransactionCommitFailedException.class);
        assertFalse(mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, dataObject));
        verify(writeTransaction, times(2)).put(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, dataObject, true);
        verify(writeTransaction, times(2)).submit();
    }

    /**
     * Test case for {@link MdsalUtils#read()}
     */
    @Test
    public void testRead() throws Exception {
        when(checkedFuture.checkedGet()).thenReturn(optional);
        when(readOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION, instanceIdentifier)).thenReturn(checkedFuture);
        /**
         * Verifies reading of specified data store. Here testing invalid scenario if particular data store object
         * not available in specified path then read() must return null only.
         */
        assertNull(mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, instanceIdentifier));
        when(optional.isPresent()).thenReturn(true);
        when(optional.get()).thenReturn(mock(DataObject.class));
        verify(readOnlyTransaction).read(LogicalDatastoreType.CONFIGURATION, instanceIdentifier);
        verify(optional).isPresent();
        /**
         * Verifies reading of specified data store. Here testing valid scenario by passing valid store and path then
         * read() returns data object if transaction is reading particular data store in that particulate path.
         */
        assertTrue(mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, instanceIdentifier) instanceof DataObject);
        verify(readOnlyTransaction, times(2)).read(LogicalDatastoreType.CONFIGURATION, instanceIdentifier);
        verify(optional, times(2)).isPresent();
        verify(optional, times(1)).get();
        /**
         * Verifies reading of specified data store.Here testing invalid scenario if any exception occurred in
         * the reading of particular data store then read() must return null only.
         */
        when(optional.isPresent()).thenThrow(ReadFailedException.class);
        assertNull(mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, instanceIdentifier));
        verify(readOnlyTransaction, times(3)).read(LogicalDatastoreType.CONFIGURATION, instanceIdentifier);
        verify(optional, times(3)).isPresent();
        verify(optional, times(1)).get();
    }
}
