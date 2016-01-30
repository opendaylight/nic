/*
 * Copyright (c) 2015 Ciena and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.cli;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.nic.impl.NicProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Redirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.allow.AllowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.block.BlockBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.log.LogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.redirect.RedirectBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.qos.constraint.QosConstraintBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Classifiers;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Command(name = "add",
         scope = "intent",
         description = "Adds an intent to the controller."
                 + "\nExamples: --actions [ALLOW] --from <subject> --to <subject>"
                 + "\nExamples: --actions [ALLOW] --from <subject> --to <subject> --constraints [QOS] --profilename<profile>"
                 + "\nExamples: --actions [LOG] --from <subject> --to <subject>"
                 + "\n          --actions [BLOCK] --from <subject>")
public class IntentAddShellCommand extends OsgiCommandSupport {

    private static final int FIRST_SUBJECT = 1;
    private static final int SECOND_SUBJECT = 2;

    protected NicConsoleProvider provider;

    @Option(name = "-f",
            aliases = { "--from" },
            description = "First subject.\n-f / --from <subject>",
            required = false,
            multiValued = false)
    String from = "any";

    @Option(name = "-t",
            aliases = { "--to" },
            description = "Second Subject.\n-t / --to <subject>",
            required = false,
            multiValued = false)
    String to = "any";

    @Option(name = "-a",
            aliases = { "--actions" },
            description = "Action to be performed.\n-a / --actions BLOCK/ALLOW/LOG",
            required = true,
            multiValued = true)
    List<String> actions = new ArrayList<String>(Arrays.asList(NicProvider.ACTION_BLOCK));

    @Option(name = "-s",
            aliases = { "--servicename" },
            description = "Service name to redirect the flow",
            required = false,
            multiValued = false)
    String serviceName = "any";

    @Option(name = "-c",
            aliases = { "--classifiers" },
            description = "Classifiers to identify application",
            required = false,
            multiValued = false)
    String classifiers = "any";

    @Option(name = "-q",
            aliases = { "--constraints" },
            description = "Constraints to be performed.\n-q / --Constraints HIGH/LOW/MEDIUM",
            required = false,
            multiValued = true)
    List<String> constraints = new ArrayList<String>(Arrays.asList(NicProvider.CONSTRAINT_QOS));

    @Option(name = "-p",
            aliases = { "--profilename" },
            description = "profile name for constraint",
            required = false,
            multiValued = false)
    String profilename;

    public IntentAddShellCommand(NicConsoleProvider provider) {
        this.provider = provider;
    }

    @Override
    protected Object doExecute() throws Exception {

        UUID uuid = UUID.randomUUID();

        List<Subjects> subjects = createSubjects();
        List<Actions> intentActions = createActions();
        List<Constraints> intentConstraints = createConstraints();
        List<Classifiers> classifiers = createClassifiers();

        Intent intent = new IntentBuilder().
                setId(new Uuid(uuid.toString()))
                .setSubjects(subjects)
                .setActions(intentActions)
                .setConstraints(intentConstraints)
                .setClassifiers(classifiers)
                .build();
        if (provider.addIntent(intent)) {
            return String.format("Intent created (id: %s)", uuid.toString());
        } else {
            return "Error creating new intent";
        }
    }

    protected List<Actions> createActions() {
        List<Actions> actionsList = new ArrayList<Actions>();

        short order = 1;
        for (String intentAction : this.actions) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action action = null;
            if (intentAction.equalsIgnoreCase(NicProvider.ACTION_ALLOW)) {
                action = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action
                        .AllowBuilder().setAllow(new AllowBuilder().build()).build();
            } else if (intentAction.equalsIgnoreCase(NicProvider.ACTION_BLOCK)) {
                action = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action
                        .BlockBuilder().setBlock(new BlockBuilder().build()).build();
            } else if (intentAction.equalsIgnoreCase(NicProvider.ACTION_LOG)) {
                action = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action
                        .LogBuilder().setLog(new LogBuilder().build()).build();
            } else if (intentAction.equalsIgnoreCase(NicProvider.ACTION_REDIRECT)) {
                Redirect actionRedirect = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action
                        .RedirectBuilder().setRedirect(new RedirectBuilder().setServiceName(serviceName).build()).build();
                action = actionRedirect;
            } else {
                continue;
            }

            Actions intentActions = new ActionsBuilder().setOrder(order).setAction(action).build();
            actionsList.add(intentActions);
            order++;
        }

        return actionsList;
    }

    protected List<Subjects> createSubjects() {
        List<Subjects> subjectList = new ArrayList<Subjects>();

        EndPointGroup endpointGroupFrom = new EndPointGroupBuilder().setName(this.from).build();
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup fromEPG =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject
                .EndPointGroupBuilder().setEndPointGroup(endpointGroupFrom).build();
        Subjects subjects1 = new SubjectsBuilder().setOrder((short) FIRST_SUBJECT).setSubject(fromEPG).build();

        EndPointGroup endpointGroupTo = new EndPointGroupBuilder().setName(this.to).build();
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup toEPG =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject
                .EndPointGroupBuilder().setEndPointGroup(endpointGroupTo).build();
        Subjects subjects2 = new SubjectsBuilder().setOrder((short) SECOND_SUBJECT).setSubject(toEPG).build();

        subjectList.add(subjects1);
        subjectList.add(subjects2);

        return subjectList;
    }

    /**
     * Returns the list of Constraints.
     */
    protected List<Constraints> createConstraints() {
        final List<Constraints> constraintsList = new ArrayList<Constraints>();
        short order = 1;
        for (String intentConstraint : this.constraints) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.Constraints constraint = null;
            if (intentConstraint.equalsIgnoreCase(NicProvider.CONSTRAINT_QOS)) {
                constraint = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints
                        .QosConstraintBuilder().setQosConstraint(new QosConstraintBuilder().setQosName(this.profilename).build()).build();
            } else {
                continue;
            }
            Constraints intentConstraints = new ConstraintsBuilder().setOrder(order).setConstraints(constraint).build();
            constraintsList.add(intentConstraints);
            order++;
        }

        return constraintsList;
    }

    /**
     * Return the list of classifiers
     */
    protected List<Classifiers> createClassifiers() {
        final List<Classifiers> classifiersList = new ArrayList<>();
        if (this.classifiers.contains(",")) {
            for (String intentClassifier : this.classifiers.split(",") ) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.classifiers.Classifiers intentClassifiers =
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.classifiers.ClassifiersBuilder()
                                .setName(intentClassifier).build();
                org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Classifiers classifiers = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent
                        .rev150122.intent.ClassifiersBuilder().setClassifiers(intentClassifiers).build();
                classifiersList.add(classifiers);
            }
        } else {
            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.classifiers.Classifiers intentClassifiers =
                    new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.classifiers.ClassifiersBuilder()
                            .setName(this.classifiers).build();
            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Classifiers classifiers = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent
                    .rev150122.intent.ClassifiersBuilder().setClassifiers(intentClassifiers).build();
            classifiersList.add(classifiers);
        }
        return classifiersList;
    }
}
