/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.statemachine.engine;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opendaylight.nic.statemachine.engine.impl.DeployFailedServiceImpl;
import org.opendaylight.nic.statemachine.engine.impl.DeployServiceImpl;
import org.opendaylight.nic.statemachine.impl.StateMachineEngineImpl;
import org.opendaylight.nic.statemachine.impl.StateMachineRendererExecutor;

import java.util.concurrent.Future;

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

    @Mock
    private StateMachineRendererExecutor rendererExecutor;

    @Mock
    private Future future;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    //TODO: [WIP] Provide valid tests
}
