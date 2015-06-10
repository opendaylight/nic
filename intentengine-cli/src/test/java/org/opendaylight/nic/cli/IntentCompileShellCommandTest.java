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

import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.assertNotNull;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * JUnit test for {@link IntentCompileShellCommand}
 *
 * IntentCompileShellCommand test class is to test
 * whether all intents compiled or not
 */
@RunWith(MockitoJUnitRunner.class)
public class IntentCompileShellCommandTest {
    /**
     * create a mock object for NicConsoleProvider class
     */
    @Mock private NicConsoleProvider nicConsole;

    /**
     * create object for IntentCompileShellCommand class
     */
    private IntentCompileShellCommand intentCompileShellCmd;

    /**
     * return a string constant when mocking the compile() function.
     */
    private final String result = "output";

    /**
     * Test method for {@link IntentCompileShellCommand#doExecute()}.
     * Test whether all intents compiled or not.
     */
    @Test
    public final void testDoExecute() {

    try {
        Mockito.when(nicConsole.compile()).thenReturn(result);
        intentCompileShellCmd = new IntentCompileShellCommand(nicConsole);
        Object output = intentCompileShellCmd.doExecute();
        assertNotNull(output);
       } catch (Exception e) {
        Assert.fail("IntentCompileShellCommand test failed..!");
       }
    }
}
