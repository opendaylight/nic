/*
 * Copyright (c) 2016 NEC Corporation.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.cli;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.QosConfig;

@RunWith(MockitoJUnitRunner.class)
public class IntentQoSConfigAddShellCommandTest {
    /**
     * Mock instance of NicConsoleProvider to perform unit testing.
     */
    @Mock private NicConsoleProvider nicConsole;

    /**
     * Instance of IntentQoSConfigAddShellCommand to perform unit testing.
     */
    private IntentQoSConfigAddShellCommand qosConfigAddShellCmd;

    /**
     * This method creates the required objects to perform unit testing.
     */
    @Before
    public void setUp() throws Exception {
        qosConfigAddShellCmd = new IntentQoSConfigAddShellCommand(nicConsole);
    }

    /**
     * Test case for {@link IntentQoSConfigAddShellCommand#doExecute}.
     */
    @Test
    public final void testDoExecute() throws Exception {
        Object feedBack = qosConfigAddShellCmd.doExecute();
            assertNotNull(feedBack);
            assertEquals("As we test a mocked object the feedback should be error message","Error creating new QoS Configuration.", feedBack);
    }

    /**
     * Test case for {@link IntentQoSConfigAddShellCommand#createQosConfig()}.
     */
    @Test
    public final void testCreateQosConfig() {
        List<QosConfig> qosConfig = qosConfigAddShellCmd.createQosConfig();
        assertNotNull(qosConfig);
        assertEquals("by default at least 2 action should be created", true, qosConfig.size() > 0);
    }
}
