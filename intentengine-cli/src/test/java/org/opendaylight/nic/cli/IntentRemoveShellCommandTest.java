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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;


/**
 * JUnit test for {@link IntentRemoveShellCommand}
 *
 * IntentRemoveShellCommand test class is to test
 * whether the specific intent is removed from controller or not
 */
@PrepareForTest(Uuid.class)
@RunWith(PowerMockRunner.class)
public class IntentRemoveShellCommandTest {

    /**
     * create a mock object for NicConsoleProvider class
     */
    @Mock private NicConsoleProvider nicConsole;
    private IntentRemoveShellCommand intentRemoveShellCmd;
    private String id;
    private Uuid uuid;

    /**
     * This method creates objects to perform unit testing.
     */
    @Before
    public void setUp() {

        /**
         * create object for IntentRemoveShellCommand class
         */
        intentRemoveShellCmd = new IntentRemoveShellCommand(nicConsole);
        /**
         * create a random UUID object in string formate.
         */
        id = UUID.randomUUID().toString();
        /**
         * create object for Uuid class.
         */
        uuid = new Uuid(id);
    }

    /**
     * Test method for
     * {@link IntentRemoveShellCommand#doExecute()}.
     *
     * test whether the specific intent is removed
     * from controller or not.
     */
    @Test
    public final void testDoExecute() throws Exception {

        PowerMockito.stub(PowerMockito.method(Uuid.class, "getDefaultInstance")).toReturn(uuid);

        /**
         * Valid scenario: removeIntent() of NicConsoleProvider returns true.
         */
        when(nicConsole.removeIntent(uuid)).thenReturn(true);
        Object output = intentRemoveShellCmd.doExecute();
        Object feedBack = "Intent successfully removed (id: Uuid [_value="+id+"])";

        /**
         * checks return value type of doExecute().
         */
        assertTrue(output instanceof Object);

        /**
         * checks intent removed or not with respective passing Uuid.
         */
        assertEquals(feedBack, output);

        /**
         * Invalid scenario: removeIntent() of NicConsoleProvider returns false.
         */
        when(nicConsole.removeIntent(uuid)).thenReturn(false);
        Object result = intentRemoveShellCmd.doExecute();
        Object expected = "Error removing intent (id: Uuid [_value="+id+"])";

        /**
         * checks return value type of doExecute().
         */
        assertTrue(result instanceof Object);

        /**
         * checks intent removed or not with respective passing Uuid.
         */
        assertEquals(expected, result);
    }

    /**
     * Method makes unused objects eligible for garbage collection
     */
    @After
    public void cleanUp() {
        intentRemoveShellCmd = null;
        id = null;
        uuid = null;
    }
}
