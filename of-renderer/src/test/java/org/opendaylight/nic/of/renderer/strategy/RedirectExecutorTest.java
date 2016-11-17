/*
 * Copyright (c) 2016 Instituto Atlântico Development Company, L.P. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.strategy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opendaylight.nic.of.renderer.impl.RedirectFlowManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author victor. Created on 17/11/16.
 */
@RunWith(PowerMockRunner.class)
public class RedirectExecutorTest {

    @InjectMocks
    private RedirectExecutor redirectExecutor;

    @Mock
    private RedirectFlowManager redirectFlowManager;

    @Mock
    private Intent intentMock;

    @Before
    public void setUp() throws Exception {
        redirectExecutor = new RedirectExecutor(redirectFlowManager);
    }

    @Test
    public void testExecuteIntentAddFlow() throws Exception {
        redirectExecutor.execute(intentMock, FlowAction.ADD_FLOW);
    }
}
