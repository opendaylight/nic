/**
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.nic.compiler.api.IntentCompiler;
import org.opendaylight.nic.compiler.api.IntentCompilerException;
import org.opendaylight.nic.compiler.api.IntentCompilerFactory;
import org.opendaylight.nic.compiler.api.Policy;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

@PrepareForTest({ FrameworkUtil.class, IntentCompilerFactory.class,
        BundleContext.class, AccessController.class })
@RunWith(PowerMockRunner.class)
/**
 * JUnit test for {@link NicProvider}.
 */
public class NicProviderTest {
    /**
     * Mock instance of DataBroker to perform unit testing.
     */
    private DataBroker mockDataBroker;
    /**
     * Mock instance of WriteTransaction to perform unit testing.
     */
    private WriteTransaction mockWriteTransaction;
    /**
     * Mock instance of ReadOnlyTransaction to perform unit testing.
     */
    private ReadOnlyTransaction mockReadOnlyTransaction;
    /**
     * Mock instance of CheckedFuture to perform unit testing.
     */
    private CheckedFuture mockListenableFuture;
    /**
     * Instance of NicProvider to perform unit testing.
     */
    private NicProvider nicProvider;
    /**
     * It creates the required objects for every unit test cases.
     *
     * @throws Exception
     */

    @Before
    public void setup() throws Exception {
        /**
         * Here creates required mock objects and defines mocking functionality
         * for mock objects.
         */
        mockListenableFuture = mock(CheckedFuture.class);
        mockWriteTransaction = mock(WriteTransaction.class);
        mockReadOnlyTransaction = mock(ReadOnlyTransaction.class);
        mockDataBroker = mock(DataBroker.class);
        when(mockWriteTransaction.submit()).thenReturn(mockListenableFuture);
        when(mockDataBroker.newWriteOnlyTransaction()).thenReturn(mockWriteTransaction);
        when(mockDataBroker.newReadOnlyTransaction()).thenReturn(mockReadOnlyTransaction);
        nicProvider = new NicProvider(mockDataBroker);
    }

    /**
     * Test case for {@link NicProvider#init()}
     */
    @Test
    public void testInit() {
        /**
         * Here creates required mock objects and defines mocking functionality
         * for mock objects.
         */
        nicProvider = spy(nicProvider);
        PowerMockito.mockStatic(FrameworkUtil.class);
        Bundle mockBundle = mock(Bundle.class);
        BundleContext mockBundleContext = mock(BundleContext.class);
        ServiceRegistration<NicConsoleProvider> mockNicConsoleRegistration = mock(ServiceRegistration.class);
        doNothing().when(nicProvider).initIntentsOperational();
        doNothing().when(nicProvider).initIntentsConfiguration();
        when(FrameworkUtil.getBundle(nicProvider.getClass())).thenReturn(mockBundle);
        when(mockBundle.getBundleContext()).thenReturn(mockBundleContext);
        when(mockBundleContext.registerService(NicConsoleProvider.class,
                nicProvider, null)).thenReturn(mockNicConsoleRegistration);
        /**
         * Here verifying init() should initialize operational and default
         * config data in MD-SAL data store.
         */
        nicProvider.init();
        assertEquals(mockNicConsoleRegistration, nicProvider.nicConsoleRegistration);
        verify(nicProvider, times(1)).initIntentsConfiguration();
        verify(mockBundle, times(1)).getBundleContext();
        verify(mockBundleContext, times(1)).registerService(NicConsoleProvider.class, nicProvider, null);
    }

    /**
     * Test case for {@link NicProvider#close()}
     */
    @Test
    public void testClose() throws Exception {
        nicProvider = spy(nicProvider);
        ServiceRegistration mockServiceRegistration = mock(ServiceRegistration.class);
        nicProvider.nicConsoleRegistration = mockServiceRegistration;
        /**
         * Here verifying close() should close active registrations.
         */
        nicProvider.close();
        verify(mockServiceRegistration, times(1)).unregister();
    }

    /**
     * Test case for {@link NicProvider#initIntentsOperational()}
     */
    @Test
    public void testInitIntentsOperational() {
        nicProvider.initIntentsOperational();
        /**
         * Here verifying initIntentsOperational() should interact with
         * DataBroker, WriteTransaction and ListenableFuture to put the Intents
         * operational data into the MD-SAL data store.
         */
        verify(mockDataBroker, times(1)).newWriteOnlyTransaction();
        verify(mockWriteTransaction, times(1)).put(
                eq(LogicalDatastoreType.OPERATIONAL),
                eq(NicProvider.INTENTS_IID), isA(Intents.class));
        verify(mockWriteTransaction, times(1)).submit();
        verify(mockListenableFuture, times(1)).addListener(isA(Runnable.class), isA(Executor.class));
    }

    /**
     * Test case for {@link NicProvider#initIntentsConfiguration()}
     */
    @Test
    public void testInitIntentsConfiguration() {
        nicProvider.initIntentsConfiguration();
        /**
         * Here verifying initIntentsOperational() should interact with
         * DataBroker, WriteTransaction to place default config data in data
         * store tree.
         */
        verify(mockDataBroker, times(1)).newWriteOnlyTransaction();
        verify(mockWriteTransaction, times(1)).put(
                eq(LogicalDatastoreType.CONFIGURATION),
                eq(NicProvider.INTENTS_IID), isA(Intents.class));
        verify(mockWriteTransaction, times(1)).submit();
    }

    /**
     * Test case for {@link NicProvider#addIntent()}
     */
    @Test
    public void testAddIntent() {
        Intent mockIntent = mock(Intent.class);
        boolean actualResult, expectedResult;
        /**
         * Here verifying addIntent() should return true if it added given
         * Intent to data store tree.
         */
        expectedResult = true;
        actualResult = nicProvider.addIntent(mockIntent);
        assertEquals(expectedResult, actualResult);
        verify(mockDataBroker, times(1)).newWriteOnlyTransaction();
        verify(mockWriteTransaction, times(1)).put(
                eq(LogicalDatastoreType.CONFIGURATION),
                eq(NicProvider.INTENTS_IID), isA(Intents.class));
        verify(mockWriteTransaction, times(1)).submit();
        /**
         * Here verifying addIntent() should return false if unable to add given
         * Intent due to some exception raised when adding given Intent to data
         * store tree.
         */
        doThrow(new RuntimeException()).when(mockWriteTransaction).put(
                eq(LogicalDatastoreType.CONFIGURATION),
                eq(NicProvider.INTENTS_IID), isA(Intents.class));
        expectedResult = false;
        actualResult = nicProvider.addIntent(mockIntent);
        assertEquals(expectedResult, actualResult);
        verify(mockDataBroker, times(2)).newWriteOnlyTransaction();
        verify(mockWriteTransaction, times(2)).put(
                eq(LogicalDatastoreType.CONFIGURATION),
                eq(NicProvider.INTENTS_IID), isA(Intents.class));
        verify(mockWriteTransaction, times(1)).submit();
    }

    /**
     * Test case for {@link NicProvider#removeIntent()}
     */
    @Test
    public void testRemoveIntent() {
        Uuid mockUuid = mock(Uuid.class);
        boolean actualResult, expectedResult;
        /**
         * Here verifying removeIntent() should return ture if it removed given
         * Intent from data store tree.
         */
        expectedResult = true;
        actualResult = nicProvider.removeIntent(mockUuid);
        assertEquals(expectedResult, actualResult);
        verify(mockDataBroker, times(1)).newWriteOnlyTransaction();
        verify(mockWriteTransaction, times(1)).delete(
                eq(LogicalDatastoreType.CONFIGURATION),
                isA(InstanceIdentifier.class));
        verify(mockWriteTransaction, times(1)).submit();
        /**
         * Here verifying removeIntent() should return false if unable to remove
         * given Intent due to some exception raised when removing given Intent
         * to data store tree.
         */
        doThrow(new RuntimeException()).when(mockWriteTransaction).delete(
                eq(LogicalDatastoreType.CONFIGURATION),
                isA(InstanceIdentifier.class));
        expectedResult = false;
        actualResult = nicProvider.removeIntent(mockUuid);
        assertEquals(expectedResult, actualResult);
        verify(mockDataBroker, times(2)).newWriteOnlyTransaction();
        verify(mockWriteTransaction, times(2)).delete(
                eq(LogicalDatastoreType.CONFIGURATION),
                isA(InstanceIdentifier.class));
        verify(mockWriteTransaction, times(1)).submit();
    }

    /**
     * Test case for {@link NicProvider#listIntents()}
     */
    @Test
    public void testListIntents() throws Exception {
        List<Intent> actualListOfIntents;
        /**
         * Here creates required mock objects and defines mocking functionality
         * for mock objects.
         */
        List<Intent> mockListOfIntentsForConfiguration = mock(List.class);
        List<Intent> mockListOfIntentsForOperational = mock(List.class);
        Intents mockIntents = mock(Intents.class);
        Optional mockOptional = mock(Optional.class);
        CheckedFuture mockCheckedFuture = mock(CheckedFuture.class);
        when(mockIntents.getIntent()).thenReturn(mockListOfIntentsForConfiguration);
        when(mockOptional.get()).thenReturn(mockIntents);
        when(mockCheckedFuture.checkedGet()).thenReturn(mockOptional);
        when(mockReadOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION,
                NicProvider.INTENTS_IID)).thenReturn(mockCheckedFuture);
        Intents mockIntentsTwo = mock(Intents.class);
        Optional mockOptionalTwo = mock(Optional.class);
        CheckedFuture mockCheckedFutureTwo = mock(CheckedFuture.class);
        when(mockIntentsTwo.getIntent()).thenReturn(mockListOfIntentsForOperational);
        when(mockOptionalTwo.get()).thenReturn(mockIntentsTwo);
        when(mockCheckedFutureTwo.checkedGet()).thenReturn(mockOptionalTwo);
        when(mockReadOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL,
                NicProvider.INTENTS_IID)).thenReturn(mockCheckedFutureTwo);
        /**
         * Here verifying listIntents() should return list of Intents from
         * configuration data store if isConfigurationDatastore is true.
         */
        actualListOfIntents = nicProvider.listIntents(true);
        assertEquals(mockListOfIntentsForConfiguration, actualListOfIntents);
        verify(mockDataBroker, times(1)).newReadOnlyTransaction();
        verify(mockReadOnlyTransaction, times(1)).read(
                LogicalDatastoreType.CONFIGURATION, NicProvider.INTENTS_IID);
        verify(mockCheckedFuture, times(1)).checkedGet();
        verify(mockOptional, times(1)).get();
        verify(mockIntents, times(1)).getIntent();
        /**
         * Here verifying listIntents() should return list of Intents from
         * operational data store if isConfigurationDatastore is false.
         */
        actualListOfIntents = nicProvider.listIntents(false);
        assertEquals(mockListOfIntentsForOperational, actualListOfIntents);
        verify(mockDataBroker, times(2)).newReadOnlyTransaction();
        verify(mockReadOnlyTransaction, times(1)).read(
                LogicalDatastoreType.OPERATIONAL, NicProvider.INTENTS_IID);
        verify(mockCheckedFutureTwo, times(1)).checkedGet();
        verify(mockOptionalTwo, times(1)).get();
        verify(mockIntentsTwo, times(1)).getIntent();
        /**
         * Here verifying listIntents() should return null if any exception
         * raised when reading Intents from specific data store.
         */
        doThrow(new RuntimeException()).when(mockReadOnlyTransaction).read(
                LogicalDatastoreType.CONFIGURATION, NicProvider.INTENTS_IID);
        actualListOfIntents = nicProvider.listIntents(true);
        assertEquals(new ArrayList(), actualListOfIntents);
        assertTrue(actualListOfIntents.isEmpty());
    }

    /**
     * Test case for {@link NicProvider#getIntent()}
     */
    @Test
    public void testGetIntent() throws Exception {
        Intent actualIntent;
        Uuid uuid = Uuid.getDefaultInstance("b9a13232-525e-4d8c-be21-cd65e3436037");
        /**
         * Here creates required mock objects and defines mocking functionality
         * for mock objects.
         */
        Intent mockIntentForConfiguration = mock(Intent.class);
        Intent mockIntentForOperational = mock(Intent.class);
        Optional mockOptional = mock(Optional.class);
        CheckedFuture mockCheckedFuture = mock(CheckedFuture.class);
        when(mockOptional.get()).thenReturn(mockIntentForConfiguration, null, mockIntentForOperational);
        when(mockCheckedFuture.checkedGet()).thenReturn(mockOptional);
        when(mockReadOnlyTransaction.read(eq(LogicalDatastoreType.CONFIGURATION),
                isA(InstanceIdentifier.class))).thenReturn(mockCheckedFuture);
        when(mockReadOnlyTransaction.read(eq(LogicalDatastoreType.OPERATIONAL),
                isA(InstanceIdentifier.class))).thenReturn(mockCheckedFuture);
        /**
         * Here verifying getIntent() should return Intent from configuration
         * data store.
         */
        actualIntent = nicProvider.getIntent(uuid);
        assertEquals(mockIntentForConfiguration, actualIntent);
        verify(mockReadOnlyTransaction, times(1)).read(
                eq(LogicalDatastoreType.CONFIGURATION),
                isA(InstanceIdentifier.class));
        verify(mockReadOnlyTransaction, times(0)).read(
                eq(LogicalDatastoreType.OPERATIONAL),
                isA(InstanceIdentifier.class));
        verify(mockCheckedFuture, times(1)).checkedGet();
        verify(mockOptional, times(1)).get();
        /**
         * Here verifying getIntent() should return Intent from operational data
         * store.
         */
        actualIntent = nicProvider.getIntent(uuid);
        assertEquals(mockIntentForOperational, actualIntent);
        verify(mockReadOnlyTransaction, times(2)).read(
                eq(LogicalDatastoreType.CONFIGURATION),
                isA(InstanceIdentifier.class));
        verify(mockReadOnlyTransaction, times(1)).read(
                eq(LogicalDatastoreType.OPERATIONAL),
                isA(InstanceIdentifier.class));
        verify(mockCheckedFuture, times(3)).checkedGet();
        verify(mockOptional, times(3)).get();
        /**
         * Here verifying getIntent() should return null if it is unable to read
         * Intent due to some exception raised at the time of reading from data
         * store.
         */
        doThrow(new RuntimeException()).when(mockReadOnlyTransaction).read(
                eq(LogicalDatastoreType.CONFIGURATION),
                isA(InstanceIdentifier.class));
        actualIntent = nicProvider.getIntent(uuid);
        assertEquals(null, actualIntent);

    }

    /**
     * Test case for {@link NicProvider#compile()}
     */
    @Test
    public void testCompile() throws Exception {
        String expectedResult, actualResult;
        List listOfIntent = new ArrayList();
        List listOfSubjects = new ArrayList();
        /**
         * Here creates required mock objects and defines mocking functionality
         * for mock objects.
         */
        IntentCompiler mockIntentCompiler = mock(IntentCompiler.class);
        Policy mockPolicy = mock(Policy.class);
        Intent mockIntent = mock(Intent.class);
        nicProvider = spy(nicProvider);
        EndPointGroup mockEndPointGroupForSource = mock(EndPointGroup.class);
        EndPointGroup mockEndPointGroupForDestination = mock(EndPointGroup.class);
        Subjects mockSubjects = mock(Subjects.class);
        Actions mockActions = mock(Actions.class);
        Action mockAllow = mock(Allow.class);
        Block mockBlock = mock(Block.class);
        Action mockAction = mock(Action.class);
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup mockEndPointGroup =
                mock(org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup.class);
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup mockEndPointGroupTwo =
                mock(org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup.class);
        IntentCompilerException mockIntentCompilerException = mock(IntentCompilerException.class);
        when(mockIntentCompiler.createPolicy(isA(Set.class), isA(Set.class),
                isA(Set.class))).thenReturn(mockPolicy);
        PowerMockito.stub(PowerMockito.method(IntentCompilerFactory.class,
                "createIntentCompiler")).toReturn(mockIntentCompiler);
        doReturn(listOfIntent).when(nicProvider).listIntents(eq(true));
        listOfIntent.add(mockIntent);
        listOfSubjects.add(mockSubjects);
        listOfSubjects.add(mockSubjects);
        when(mockSubjects.getSubject()).thenReturn(mockEndPointGroupForSource, mockEndPointGroupForDestination);
        when(mockIntent.getSubjects()).thenReturn(listOfSubjects);
        when(mockActions.getAction()).thenReturn(mockAllow, mockBlock, mockAction, mockBlock);
        List listOfActions = new ArrayList();
        listOfActions.add(mockActions);
        when(mockIntent.getActions()).thenReturn(listOfActions);
        when(mockEndPointGroup.getName()).thenReturn("Mock EndPointGroupOne");
        when(mockEndPointGroupTwo.getName()).thenReturn("Mock EndPointGroupTwo");
        when(mockEndPointGroupForSource.getEndPointGroup()).thenReturn(mockEndPointGroup);
        when(mockEndPointGroupForDestination.getEndPointGroup()).thenReturn(mockEndPointGroupTwo);

        /**
         * Here verifying compile() should return specific pattern string if
         * action is of type allow.
         */
        expectedResult = ">>> Original policies:\n" + mockPolicy + "\n\n>>> Compiled policies:\n";
        actualResult = nicProvider.compile();
        assertEquals(expectedResult, actualResult);
        /**
         * Here verifying compile() should return specific pattern string if
         * action is of type block.
         */
        expectedResult = ">>> Original policies:\n" + mockPolicy + "\n\n>>> Compiled policies:\n";
        actualResult = nicProvider.compile();
        assertEquals(expectedResult, actualResult);
        /**
         * Here verifying compile() should return specific pattern string if
         * action is not either allow type nor block type.
         */
        expectedResult = "[ERROR] Invalid action: " + mockAction.getClass().getName();
        actualResult = nicProvider.compile();
        assertEquals(expectedResult, actualResult);
        /**
         * Here verifying compile() should return specific pattern string if any
         * exception raised during the compiling the given policies.
         */
        List<Policy> listOfPolicy = new ArrayList();
        listOfPolicy.add(mockPolicy);
        when(mockIntentCompilerException.getMessage()).thenReturn("mockito msg..");
        when(mockIntentCompilerException.getRelatedPolicies()).thenReturn(listOfPolicy);
        doThrow(mockIntentCompilerException).when(mockIntentCompiler).compile(isA(Collection.class));
        expectedResult = "[ERROR] Compilation failure: mockito msg..\nRelated policies:\n    "
                + mockPolicy + "\n";
        actualResult = nicProvider.compile();
        assertEquals(expectedResult, actualResult);
        /**
         * Here verifying compile() should return specific pattern string if any
         * exception raised during the parsing the endpointgroup with
         * destination subject.
         */
        when(mockSubjects.getSubject()).thenReturn(mockEndPointGroupForSource, mockEndPointGroupForDestination);
        when(mockIntentCompiler.parseEndpointGroup("Mock EndPointGroupTwo")).thenThrow(new UnknownHostException(""));
        expectedResult = "[ERROR] Invalid subject: " + "Mock EndPointGroupTwo";
        actualResult = nicProvider.compile();
        assertEquals(expectedResult, actualResult);
        /**
         * Here verifying compile() should return specific pattern string if any
         * exception raised during the parsing the endpointgroup with source
         * subject.
         */
        when(mockSubjects.getSubject()).thenReturn(mockEndPointGroupForSource, mockEndPointGroupForDestination);
        when(mockIntentCompiler.parseEndpointGroup(eq("Mock EndPointGroupOne"))).thenThrow(new UnknownHostException(""));
        expectedResult = "[ERROR] Invalid subject: " + "Mock EndPointGroupOne";
        actualResult = nicProvider.compile();
        assertEquals(expectedResult, actualResult);
    }

    /**
     * Test case for {@link NicProvider#formatPolicies()}
     */
    @Test
    public void testFormatPolicies() throws Exception {
        Collection<Policy> listOfPolicy = new ArrayList<Policy>();
        /**
         * Here creates required mock objects and defines mocking functionality
         * for mock objects.
         */
        Policy mockPolicyOne, mockPolicyTwo;
        mockPolicyOne = mock(Policy.class);
        mockPolicyTwo = mock(Policy.class);
        listOfPolicy.add(mockPolicyOne);
        listOfPolicy.add(mockPolicyTwo);
        /**
         * Here verifying formatPolicies() should return specific pattern string
         * by reading given policies.
         */
        String actualResult, expectedResult;
        expectedResult = mockPolicyOne + "\n" + mockPolicyTwo + "\n";
        actualResult = Whitebox.invokeMethod(nicProvider, "formatPolicies", listOfPolicy);
        assertEquals(expectedResult, actualResult);
    }
}
