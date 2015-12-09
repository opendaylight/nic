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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opendaylight.nic.engine.impl.DeployFailedServiceImpl;
import org.opendaylight.nic.engine.impl.DeployServiceImpl;
import org.opendaylight.nic.impl.StateMachineEngineImpl;
import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * JUnit test for {@link StateMachineEngineImpl}
 */
public class DeployServiceTest {

    @Spy
    private StateMachineEngineImpl engineService;

    @Mock
    private DeployServiceImpl deployService;

    @Mock
    private DeployFailedServiceImpl failedService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecuteDeployingWithNodeRemovedEvent() {
        engineService.execute(Intent.State.DEPLOYING, EventType.NODE_REMOVED);
        verify(engineService, times(1)).changeState(Intent.State.DEPLOYING);
    }

    @Test
    public void testExecuteDeployingWithNodeUpEvent() {
        engineService.execute(Intent.State.DEPLOYING, EventType.NODE_ADDED);
        verify(engineService, times(1)).changeState(Intent.State.DEPLOYING);
    }

    @Test
    public void testExecuteUndeployingWithNodeDownEvent() {
        engineService.execute(Intent.State.UNDEPLOYING, EventType.NODE_REMOVED);
        verify(engineService, times(1)).changeState(Intent.State.UNDEPLOYING);
    }

    @Test
    public void testExecuteDeployFailedWithNodeUpEvent() {
        engineService.execute(Intent.State.DEPLOYFAILED, EventType.NODE_ADDED);
        verify(engineService, times(1)).changeState(Intent.State.DEPLOYFAILED);
    }

    @Test
    public void testExecuteDisablingWithAnyEvent() {
        engineService.execute(Intent.State.DISABLING, EventType.INTENT_REMOVED);
        verify(engineService, times(1)).changeState(Intent.State.DISABLING);
    }
}
