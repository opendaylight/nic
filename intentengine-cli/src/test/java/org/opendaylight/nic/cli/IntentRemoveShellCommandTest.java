/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.cli;

import java.util.UUID;

import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.assertNotNull;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

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
    /**
     * create object for IntentRemoveShellCommand class
     */
    private IntentRemoveShellCommand intentRemoveShellCmd;
    /**
     * create UUID object in string formate.
     */
     private final String uuid = UUID.randomUUID().toString();

    /**
     * This method creates the IntentRemoveShellCommand object to perform unit testing.
     */
    @Before
    public void setUp() {
        intentRemoveShellCmd = new IntentRemoveShellCommand(nicConsole);
    }

    /**
     * Test method for
     * {@link IntentRemoveShellCommand#doExecute()}.
     *
     * test whether the specific intent is removed
     * from controller or not.
     */
    @Test
    public final void testDoExecute() {

        PowerMockito.stub(PowerMockito.method(Uuid.class, "getDefaultInstance")).toReturn(new Uuid(uuid));
        /**
         * Success case : removeIntent() of NicConsoleProvider returns true.
         */
        try {
            Mockito.when(nicConsole.removeIntent(Mockito.isA(Uuid.class))).thenReturn(true);
            Object output = intentRemoveShellCmd.doExecute();
            assertNotNull(output);
        } catch (Exception e)
        {
            Assert.fail("IntentRemoveShellCommand test failed..!");
        }
        /**
         * Failure case : removeIntent() of NicConsoleProvider returns false.
         */
        try {
            Mockito.when(nicConsole.removeIntent(Mockito.isA(Uuid.class))).thenReturn(false);
            Object output = intentRemoveShellCmd.doExecute();
            assertNotNull(output);
        } catch (Exception e)
        {
            Assert.fail("IntentRemoveShellCommand test failed..!");
        }
    }

    /**
     * This method makes intentRemoveShellCmd object eligible for garbage collection
     */
    @After
    public void cleanUp() {
        intentRemoveShellCmd = null;
    }
}
