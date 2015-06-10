/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.cli;

import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * JUnit test for {@link IntentListShellCommand}
 */
@RunWith(MockitoJUnitRunner.class)
public class IntentListShellCommandTest {
    /**
     * create a mock object for NicConsoleProvider class
     */
    @Mock private NicConsoleProvider provider;
    /**
     * create a mock object for Intent class
     */
    @Mock private Intent intent;
    /**
     * create a mock object for Uuid class
     */
    @Mock private Uuid uuid;

    boolean isConfigurationData = false;
    /*
     * Object for the class IntentListShellCommand
     */
    private IntentListShellCommand intentListShellCommand;

    @Before
    public void setUp() throws Exception {
        intentListShellCommand = new IntentListShellCommand(provider);
    }

    /**
     * Test method for
     * {@link IntentListShellCommand#doExecute()}.
     *
     * test whether all intents Listed or not.
     *
     */
    @Test
    public final void testDoExecute() throws Exception {
        Mockito.when(intent.getId()).thenReturn(uuid);

        //passing empty intentlist
        ArrayList intentlist = new ArrayList();
        Mockito.when(provider.listIntents(isConfigurationData)).thenReturn(intentlist);
        intentListShellCommand.doExecute();

        intentlist.add(intent);
        intentListShellCommand.doExecute();
    }
}
