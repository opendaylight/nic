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
import org.opendaylight.nic.impl.StateMachineRendererExecutor;
import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent;

import java.util.concurrent.Future;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
