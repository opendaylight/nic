package org.opendaylight.nic.cli;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import static org.junit.Assert.*;

import org.apache.felix.service.command.CommandSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

@RunWith(MockitoJUnitRunner.class)
public class IntentAddShellCommandTest {

    @Mock private NicConsoleProvider nicConsole;
    @Mock private CommandSession cmdSession;

    private IntentAddShellCommand nicAddShellCmd;

    @Before
    public void setUp() throws Exception {
        nicAddShellCmd = new IntentAddShellCommand(nicConsole);
    }

    @Test
    public final void testDoExecute() throws Exception {
        Object feedBack = nicAddShellCmd.doExecute();
            assertNotNull(feedBack);
            assertEquals("As we test a mocked object the feedback should be error message","Error creating new intent", feedBack);
    }

    @Test
    public final void testCreateActions() {
        List<Actions> actions = nicAddShellCmd.createActions();
        assertNotNull(actions);
        assertEquals("by default at least 1 action should be created", true, actions.size() > 0);
    }

    @Test
    public final void testCreateSubjects() {
        List<Subjects> subjects = nicAddShellCmd.createSubjects();
        assertNotNull(subjects);
        assertEquals("there are should be at least 2 subjects", true , subjects.size() > 1);
    }

    @Test
    public final void testExecute() throws Exception {
        assertNotNull(nicAddShellCmd.execute(cmdSession));
    }

}
