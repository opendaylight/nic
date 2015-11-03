/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.cli;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

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
    /**
     * Object for the class IntentListShellCommand
     */
    private IntentListShellCommand intentListShellCommand;
    /**
     * Source for intentID
     */
    private static String intentID;

    @Before
    public void setUp() throws Exception {
        intentListShellCommand = new IntentListShellCommand(provider);
        intentID = UUID.randomUUID().toString();
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

        boolean isConfigurationData = false;
        final String expectedID = "#1 - id: "+intentID+"\n";
        final String failResult = "No intents found. Check the logs for more details.";
        final String noIntent = "No intent found";

        /**
         * Invalid scenario - passing empty intent list and returns No intents found.
         */
        final List<Intent> intentlist = new ArrayList<Intent>();
        Object feedBack = intentListShellCommand.doExecute();
        assertEquals(noIntent, failResult, feedBack);

        /**
         * Valid scenario - passing intent list and returns intent ID
         */
        when(intent.getId()).thenReturn(uuid);
        when(uuid.getValue()).thenReturn(intentID);
        intentlist.add(intent);
        when(provider.listIntents(isConfigurationData)).thenReturn(intentlist);
        Object result = intentListShellCommand.doExecute();
        assertEquals(expectedID, result);
    }

    /**
     * Unused objects eligible for garbage collection.
     */
    @After
    public void tearDown() throws Exception {
        intentListShellCommand = null;
        intentID = null;
    }
}
