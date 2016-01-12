/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.nemo.renderer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.UserId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.BeginTransactionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.BeginTransactionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.CommonRpcResult.ResultCode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.EndTransactionInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.EndTransactionOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.NemoIntentService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.RegisterUserInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.RegisterUserOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.StructureStyleNemoUpdateInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.StructureStyleNemoUpdateOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.Users;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.users.User;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.users.UserBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.users.UserKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;

/**
 * Unit test class for {@link NEMORenderer}.
 */
@PrepareForTest({ NEMORenderer.class, FrameworkUtil.class })
@RunWith(PowerMockRunner.class)
public class NEMORendererTest {
    /**
     * Object for the class NEMORenderer.
     */
    private NEMORenderer nemoRenderer;

    /**
     * create a mock object for DataBroker class.
     */
    @Mock
    private DataBroker dataBroker;

    /**
     * create a mock object for RpcProviderRegistry class.
     */
    @Mock
    private RpcProviderRegistry rpcProviderRegistry;

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
    ServiceRegistration<NEMORenderer> nicConsoleRegistration;

    /**
     * create a mock Object for the class ListenerRegistration.
     */
    @Mock
    ListenerRegistration<DataChangeListener> nemoRendererListenerRegistration;

    /**
     * create a mock Object for the class WriteTransaction.
     */
    @Mock
    WriteTransaction writeTransactionMock;

    /**
     * create a mock Object for the class ReadTransaction.
     */
    @Mock
    ReadOnlyTransaction readTransactionMock;

    /**
     * create a mock Object for the class ReadWriteTransaction.
     */
    @Mock
    ReadWriteTransaction readWriteTransactionMock;

    /**
     * create a mock Object for the class CheckedFuture.
     */
    @Mock
    CheckedFuture<Void, TransactionCommitFailedException> checkedFuture;

    /**
     * create a mock Object for the class NemoIntentService.
     */
    @Mock
    NemoIntentService nemoEngine;

    @Mock
    CheckedFuture<Optional<User>, ReadFailedException> mockUserFuture;

    /**
     * Initial set up.
     */
    @Before
    public void setUp() {
        /**
         * Here creates objects and defines mocking functionality for mock objects.
         */
        PowerMockito.mockStatic(FrameworkUtil.class);
        when(FrameworkUtil.getBundle(NEMORenderer.class)).thenReturn(bundle);
        when(bundle.getBundleContext()).thenReturn(context);
        when(context.registerService(NEMORenderer.class, nemoRenderer, null)).thenReturn(nicConsoleRegistration);

        when(
                dataBroker.registerDataChangeListener(eq(LogicalDatastoreType.CONFIGURATION),
                        eq(NEMORenderer.INTENT_IID), isA(DataChangeListener.class), eq(DataChangeScope.SUBTREE)))
                .thenReturn(nemoRendererListenerRegistration);
        when(rpcProviderRegistry.getRpcService(NemoIntentService.class)).thenReturn(nemoEngine);

        when(writeTransactionMock.submit()).thenReturn(checkedFuture);
        when(dataBroker.newWriteOnlyTransaction()).thenReturn(writeTransactionMock);
        when(dataBroker.newReadOnlyTransaction()).thenReturn(readTransactionMock);
        when(dataBroker.newReadWriteTransaction()).thenReturn(readWriteTransactionMock);
        nemoRenderer = new NEMORenderer(dataBroker, rpcProviderRegistry);

    }

    /**
     * Test method for {@link NEMORenderer#init()}.
     */
    @Test
    public void testInit() {
        /**
         * It should initialize operational and default config data in MD-SAL data store.
         */
        nemoRenderer.init();

        /**
         * Verifying nemoRendere has been registered as a DataChangeListerner.
         */
        // verify(context).registerService(NEMORenderer.class, nemoRenderer, null);
        verify(dataBroker).registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, NEMORenderer.INTENT_IID,
                nemoRenderer, DataChangeScope.SUBTREE);
        PowerMockito.verifyStatic(times(1));
    }

    /**
     * Test
     *
     * @throws ExecutionException
     * @throws InterruptedException
     *
     * @throws Exception
     */
    @Test
    public void testCreateOrUpdateIntent() throws InterruptedException, ExecutionException {

        when(nemoEngine.registerUser(any(RegisterUserInput.class))).thenReturn(
                RpcResultBuilder.success(new RegisterUserOutputBuilder().setResultCode(ResultCode.Ok)).buildFuture());

        when(nemoEngine.beginTransaction(any(BeginTransactionInput.class))).thenReturn(
                RpcResultBuilder.success(new BeginTransactionOutputBuilder().setResultCode(ResultCode.Ok))
                        .buildFuture());
        when(nemoEngine.structureStyleNemoUpdate(any(StructureStyleNemoUpdateInput.class))).thenReturn(
                RpcResultBuilder.success(new StructureStyleNemoUpdateOutputBuilder().setResultCode(ResultCode.Ok))
                        .buildFuture());
        when(nemoEngine.endTransaction(any(EndTransactionInput.class))).thenReturn(
                RpcResultBuilder.success(new EndTransactionOutputBuilder().setResultCode(ResultCode.Ok)).buildFuture());

        IntentKey intentKey = new IntentKey(new Uuid(UUID.randomUUID().toString()));
        InstanceIdentifier<User> userPath = InstanceIdentifier.builder(Users.class)
                .child(User.class, new UserKey(new UserId(intentKey.getId().getValue()))).build();
        when(readTransactionMock.read(LogicalDatastoreType.CONFIGURATION, userPath)).thenReturn(mockUserFuture);
        when(mockUserFuture.get()).thenReturn(Optional.of(new UserBuilder().build()));

        nemoRenderer.init();

        // empty intent
        Intent emptyIntent = new IntentBuilder().setKey(intentKey).build();
        assertFalse("Should not be able to process empty intent", nemoRenderer.createOrUpdateIntent(emptyIntent));
        verifyZeroInteractions(nemoEngine);

        // BandwidthOnDemand intent
        Intent intent = NEMOIntentParserTest.getBandwidthOnDemandIntent(intentKey);
        assertTrue("Should be able to process BoD intent", nemoRenderer.createOrUpdateIntent(intent));
        verify(nemoEngine).beginTransaction(any(BeginTransactionInput.class));
        verify(nemoEngine).structureStyleNemoUpdate(any(StructureStyleNemoUpdateInput.class));
        verify(nemoEngine).endTransaction(any(EndTransactionInput.class));
    }

    /**
     * Test method for {@link nemoRenderer#Close()}.
     *
     * @throws Exception
     */
    @Test
    public void testClose() throws Exception {
        /**
         * Valid scenario - it should initialize operational and default config data in MD-SAL data store and should
         * close listeners properly. Verifying nicConsoleRegistration object invoking unregister method. Verifying
         * nemoRendererListener object invoking close method.
         */
        nemoRenderer.init();
        nemoRenderer.close();
        verify(nemoRendererListenerRegistration).close();
    }

}
