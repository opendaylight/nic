/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertNotNull;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import org.opendaylight.nic.api.NicConsoleProvider;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.ActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.SubjectsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.AllowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.BlockBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup;

/**
 * JUnit test for {@link IntentShowShellCommand}.
 */
@PrepareForTest(Uuid.class)
@RunWith(PowerMockRunner.class)
public class IntentShowShellCommandTest {
    /**
     * create mock object for NicConsoleProvider class
     */
    @Mock private NicConsoleProvider provider;

    /**
     * create object for IntentShowShellCommand class
     */
    private IntentShowShellCommand intentShowShellCommand;

    /**
     * String declaration for source IP address
     */
    private static final String SOURCE_IP = "10.1.1.1";

    /**
     * String declaration for destination IP address
     */
    private static final String DEST_IP = "10.1.1.2";

    @Before
    public void init() throws Exception {
        intentShowShellCommand = new IntentShowShellCommand(provider);
    }

    /**
     * Test method for {@link IntentShowShellCommand#doExecute()}.
     *
     * @throws Exception  An error occurred
     */
    @Test
    public final void testDoExecute() throws Exception {
        String id = UUID.randomUUID().toString();
        IntentKey intentkey = new IntentKey(new Uuid(id));
        Allow allow = new AllowBuilder().build();
        Block block = new BlockBuilder().build();
        Actions actionAllow = new ActionsBuilder().setAction(allow).build();
        Actions actionBlock = new ActionsBuilder().setAction(block).build();
        Actions actionNull = new ActionsBuilder().setAction(null).build();
        List<Actions> actionlist = new ArrayList<Actions>();
        actionlist.add(actionAllow);
        actionlist.add(actionBlock);
        actionlist.add(actionNull);

        EndPointGroup src = new EndPointGroupBuilder().setName(SOURCE_IP).build();
        EndPointGroup dest = new EndPointGroupBuilder().setName(DEST_IP).build();
        Subject srcAddress = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder().setEndPointGroup(src).build();
        Subject destAddress = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder().setEndPointGroup(dest).build();

        Subjects srcsubject = new SubjectsBuilder().setSubject(srcAddress).build();
        Subjects destsubject = new SubjectsBuilder().setSubject(destAddress).build();
        List<Subjects> subjectslist = new ArrayList<Subjects>();
        subjectslist.add(srcsubject);
        subjectslist.add(destsubject);

        Intent intent = new IntentBuilder().setKey(intentkey).setActions(actionlist).setSubjects(subjectslist).build();
        PowerMockito.stub(PowerMockito.method(Uuid.class, "getDefaultInstance")).toReturn(new Uuid(id));
        Mockito.when(provider.getIntent(Mockito.isA(Uuid.class))).thenReturn(intent);
        Object output = intentShowShellCommand.doExecute();           
        assertNotNull(output);

        Mockito.when(provider.getIntent(Mockito.isA(Uuid.class))).thenReturn(null);
        Object dummy = intentShowShellCommand.doExecute();
        assertNotNull(dummy);
    }

    /**
     * Unused objects eligible for garbage collection.
     */
    @After
    public void cleanUp() {
        intentShowShellCommand = null;
    }
}


