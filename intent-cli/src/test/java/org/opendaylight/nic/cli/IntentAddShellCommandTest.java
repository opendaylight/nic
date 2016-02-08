package org.opendaylight.nic.cli;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.InvalidObjectException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.apache.felix.service.command.CommandSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

@RunWith(MockitoJUnitRunner.class)
public class IntentAddShellCommandTest {

    @Mock private NicConsoleProvider nicConsole;
    @Mock private IntentMappingService mappingService;
    @Mock private CommandSession cmdSession;

    private IntentAddShellCommand nicAddShellCmd;

    @Before
    public void setUp() throws Exception {
        nicAddShellCmd = new IntentAddShellCommand(nicConsole, mappingService);
    }

    @Test
    public final void testDoExecute() throws Exception {
        Object feedBack = nicAddShellCmd.doExecute();
            assertNotNull(feedBack);
            assertEquals("As we test a mocked object the feedback should be error message","No subject found with value: any in the mapping service. Try adding it using: intent:map' command", feedBack);
    }

    @Test
    public final void testCreateActions() {
        List<Actions> actions = nicAddShellCmd.createActions();
        assertNotNull(actions);
        assertEquals("by default at least 1 action should be created", true, actions.size() > 0);
    }

    @Test
    public final void testCreateSubjects() throws InvalidObjectException {

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("any","0.0.0.0");

        when(mappingService.get("any")).thenReturn(resultMap);
        when(mappingService.get("any")).thenReturn(resultMap);

        List<Subjects> subjects = nicAddShellCmd.createSubjects();
        assertNotNull(subjects);
        assertEquals("there are should be at least 2 subjects", true , subjects.size() > 1);
    }

    @Test
    public final void testExecute() throws Exception {
        assertNotNull(nicAddShellCmd.execute(cmdSession));
    }

    /**
     * Test case for {@link IntentAddShellCommand#createConstraints()}.
     */
    @Test
    public final void testCreateConstraints() throws InvalidObjectException {
        List<Constraints> constraints = nicAddShellCmd.createConstraints();
        assertNotNull(constraints);
        assertEquals("there are should be at least 1 constraints", true , constraints.size() > 0);
    }

}
