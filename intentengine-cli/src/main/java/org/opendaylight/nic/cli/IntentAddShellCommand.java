/*
 * Copyright (c) 2015 Ciena and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.ActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.SubjectsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.AllowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.BlockBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

@Command(name = "add", scope = "intent", description = "Adds an intent to the controller.")
public class IntentAddShellCommand extends OsgiCommandSupport {

    private static final int FIRST_SUBJECT = 1;
    private static final int SECOND_SUBJECT = 2;

    protected NicConsoleProvider provider;

    @Argument(index = 0, name = "subjectFrom", description = "First subject (from). Default: any.", required = false, multiValued = false)
    String subjectFrom = "any";

    @Argument(index = 1, name = "subjectTo", description = "Second Subject (to). Default: any.", required = false, multiValued = false)
    String subjectTo = "any";

    @Argument(index = 2, name = "actions", description = "Action to be performed", required = true, multiValued = true)
    List<String> actions = new ArrayList<>();

    public IntentAddShellCommand(NicConsoleProvider provider) {
        this.provider = provider;
    }

    @Override
    protected Object doExecute() throws Exception {

        UUID uuid = UUID.randomUUID();

        List<Subjects> subjects = createSubjects();
        List<Actions> actions = createActions();

        Intent intent = new IntentBuilder().setId(new Uuid(uuid.toString())).setSubjects(subjects).setActions(actions).build();
        if (provider.addIntent(intent))
            return String.format("Intent created (id: %s)", uuid.toString());
        else
            return new String("Error creating new intent");
    }

    protected List<Actions> createActions() {
        List<Actions> actionsList = new ArrayList<Actions>();

        short order = 1;
        for (String a : this.actions) {
            Action action = (a.equalsIgnoreCase("ALLOW")) ? new AllowBuilder().build() : new BlockBuilder().build();
            Actions actions = new ActionsBuilder().setOrder(order).setAction(action).build();
            actionsList.add(actions);
        }

        return actionsList;
    }

    protected List<Subjects> createSubjects() {
        List<Subjects> subjectList = new ArrayList<Subjects>();

        EndPointGroup endpointGroupFrom = new EndPointGroupBuilder().setName(subjectFrom).build();
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup from = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder().setEndPointGroup(endpointGroupFrom).build();
        Subjects subjects1 = new SubjectsBuilder().setOrder((short) FIRST_SUBJECT).setSubject(from).build();

        EndPointGroup endpointGroupTo = new EndPointGroupBuilder().setName(subjectTo).build();
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup to = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder().setEndPointGroup(endpointGroupTo).build();
        Subjects subjects2 = new SubjectsBuilder().setOrder((short) SECOND_SUBJECT).setSubject(to).build();

        subjectList.add(subjects1);
        subjectList.add(subjects2);

        return subjectList;
    }
}
