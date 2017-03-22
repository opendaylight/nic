/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.engine;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.opendaylight.nic.engine.service.TransactionHandlerService;
import org.opendaylight.nic.impl.StateMachineEngineImpl;
import org.opendaylight.nic.impl.TransactionHandlerServiceImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;

/**
 * JUnit test for {@link StateMachineEngineImpl}
 */
public class DeployServiceTest {

    private StateMachineEngineImpl engineService;
    private TransactionHandlerService transactionHandlerServiceMock;
    private IntentStateTransaction stateTransactionMock;

    @Before
    public void setUp() throws Exception {
        transactionHandlerServiceMock = mock(TransactionHandlerServiceImpl.class);
        stateTransactionMock = mock(IntentStateTransaction.class);
        when(stateTransactionMock.getIntentId()).thenReturn(UUID.randomUUID().toString());
        MockitoAnnotations.initMocks(this);
        engineService = spy(new StateMachineEngineImpl(transactionHandlerServiceMock));
    }

    @Test
    public void testExecuteDeployingWithNodeRemovedEvent() throws ExecutionException {
        when(stateTransactionMock.getCurrentState()).thenReturn("DEPLOYING");
        when(stateTransactionMock.getReceivedEvent()).thenReturn("INTENT_ADDED");
        engineService.execute(stateTransactionMock);
        verify(engineService, times(1)).changeState(stateTransactionMock);
    }

    @Test
    public void testExecuteDeployingWithNodeUpEvent() throws ExecutionException {
        when(stateTransactionMock.getCurrentState()).thenReturn("DEPLOYING");
        when(stateTransactionMock.getReceivedEvent()).thenReturn("NODE_ADDED");
        engineService.execute(stateTransactionMock);
        verify(engineService, times(1)).changeState(stateTransactionMock);
    }

    @Test
    public void testExecuteUndeployingWithNodeDownEvent() throws ExecutionException {
        when(stateTransactionMock.getCurrentState()).thenReturn("UNDEPLOYING");
        when(stateTransactionMock.getReceivedEvent()).thenReturn("NODE_REMOVED");
        engineService.execute(stateTransactionMock);
        verify(engineService, times(1)).changeState(stateTransactionMock);
    }

    @Test
    public void testExecuteDeployFailedWithNodeUpEvent() throws ExecutionException {
        when(stateTransactionMock.getCurrentState()).thenReturn("DEPLOYFAILED");
        when(stateTransactionMock.getReceivedEvent()).thenReturn("NODE_ADDED");
        engineService.execute(stateTransactionMock);
        verify(engineService, times(1)).changeState(stateTransactionMock);
    }

    @Test
    public void testExecuteDisablingWithAnyEvent() throws ExecutionException {
        when(stateTransactionMock.getCurrentState()).thenReturn("DISABLING");
        when(stateTransactionMock.getReceivedEvent()).thenReturn("INTENT_REMOVED");
        engineService.execute(stateTransactionMock);
        verify(engineService, times(1)).changeState(stateTransactionMock);
    }
}
