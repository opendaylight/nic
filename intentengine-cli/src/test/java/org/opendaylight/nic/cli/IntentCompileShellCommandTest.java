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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.nic.api.NicConsoleProvider;

/**
 * JUnit test for {@link IntentCompileShellCommand}
 * 
 * IntentCompileShellCommand test class is to test whether all intents compiled
 * or not
 */
@RunWith(MockitoJUnitRunner.class)
public class IntentCompileShellCommandTest {

    /**
     * Create a mock object for NicConsoleProvider class
     */
    @Mock
    private NicConsoleProvider nicConsole;

    /**
     * Create object for IntentCompileShellCommand class
     */
    private IntentCompileShellCommand intentCompileShellCmd;

    /**
     * Declare an expected result.
     */
    private final StringBuilder expected = new StringBuilder();

    /**
     * Test method for {@link IntentCompileShellCommand#doExecute()}.
     * 
     * Test whether all intents compiled or not.
     */
    @Test
    public final void testDoExecute() throws Exception {

        expected.append(">>> Original policies:\n");
        expected.append("from [10.1.1.1] to [10.1.1.2] apply [ALLOW]\n");
        expected.append('\n');
        expected.append(">>> Compiled policies:\n");
        expected.append("from [10.1.1.1] to [10.1.1.2] apply [ALLOW]\n");

        /**
         * return a string constant when mocking the compile() function.
         */
        final String result = expected.toString();

        when(nicConsole.compile(false)).thenReturn(result);

        intentCompileShellCmd = new IntentCompileShellCommand(nicConsole);
        Object output = intentCompileShellCmd.doExecute();

        /**
         * checks the return value type of doExecute().
         */
        assertTrue(output instanceof Object);

        /**
         * checks return value of doExecute() with expected result.
         */
        assertEquals(expected.toString(), output);
    }
}
