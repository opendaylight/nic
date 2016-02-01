/*
 * Copyright (c) 2015 Ciena and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.cli;

import com.google.gson.Gson;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.nic.constraints.ClassifierConstraint;
import org.opendaylight.nic.constraints.QualityOfServiceConstraint;
import org.opendaylight.nic.impl.NicProvider;
import org.opendaylight.nic.mapped.MappedObject;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Redirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.allow.AllowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.block.BlockBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.log.LogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.redirect.RedirectBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.QosConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.qos.constraint.QosConstraintBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.classification.constraint.ClassificationConstraintBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

import java.io.InvalidObjectException;
import java.util.*;

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
    private static final String ANY = "any";

    protected NicConsoleProvider provider;
    protected IntentMappingService mappingService;

    @Option(name = "-f",
            aliases = { "--from" },
            description = "First subject.\n-f / --from <subject>",
            required = false,
            multiValued = false)
    String from = ANY;

    @Option(name = "-t",
            aliases = { "--to" },
            description = "Second Subject.\n-t / --to <subject>",
            required = false,
            multiValued = false)
    String to = ANY;

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
    String serviceName = ANY;

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

    public IntentAddShellCommand(NicConsoleProvider provider, IntentMappingService mappingService) {
        this.provider = provider;
        this.mappingService = mappingService;
    }

    @Override
    protected Object doExecute() throws Exception {

        UUID uuid = UUID.randomUUID();
        List<Subjects> subjects = null;
        List<Actions> intentActions = null;
        List<Constraints> intentConstraints = null;

        try {
            subjects = createSubjects();
            intentActions = createActions();
            intentConstraints = createConstraints();
        }
        catch(InvalidObjectException e) {
            return e.getMessage();
        }

        Intent intent = new IntentBuilder().
                setId(new Uuid(uuid.toString()))
                .setSubjects(subjects)
                .setActions(intentActions)
                .setConstraints(intentConstraints)
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

    private String buildSubjectNotFoundMessage(String subject) {
        if(subject == null || subject.isEmpty()) {
            subject = "(empty)";
        }

        return "No subject found with value: " + subject + " in the mapping service. Try adding it using: intent:map' command";
    }

    private String buildConstraintNotFoundMessage(String constraint) {
        if(constraint == null || constraint.isEmpty()) {
            constraint = "(empty)";
        }

        return "No constraint found with value: " + constraint + " in the mapping service. Try adding it using: intent:map' command";
    }

    protected List<Subjects> createSubjects() throws InvalidObjectException {
        List<Subjects> subjectList = new ArrayList<Subjects>();

        if(mappingService.get(this.from).isEmpty())
            throw new InvalidObjectException(buildSubjectNotFoundMessage(this.from));

        if(mappingService.get(this.to).isEmpty())
            throw new InvalidObjectException(buildSubjectNotFoundMessage(this.to));

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

    private <T extends MappedObject> T extractMappedObject(Map<String, String> mappedConstraint, Class<T> type) {
        Gson gson = new Gson();
        T object =  null;

        for (String value : mappedConstraint.values()) {
            object =  gson.fromJson(value, type);
            break;
        }

        return object;
    }

    /**
     * Returns the list of Constraints.
     */
    // TODO classifiers need to be mapped to the intent map. Make a check in the Mapping service for classifiers
    protected List<Constraints> createConstraints() throws InvalidObjectException {
        final List<Constraints> constraintsList = new ArrayList<Constraints>();
        short order = 1;
        for (String intentConstraint : this.constraints) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.Constraints constraint = null;

            Map<String, String> mappedConstraint = mappingService.get(intentConstraint);

            if(mappedConstraint.isEmpty())
                throw new InvalidObjectException(buildConstraintNotFoundMessage(intentConstraint));

            MappedObject mappedObject = null;
            if((mappedObject = extractMappedObject(mappedConstraint, MappedObject.class)) == null)
                throw new InvalidObjectException(buildConstraintNotFoundMessage(intentConstraint));

            if (mappedObject.type().equals(QualityOfServiceConstraint.type)) {
                constraint = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints
                        .QosConstraintBuilder().setQosConstraint(new QosConstraintBuilder().setQosName(this.profilename).build()).build();
            } else if (mappedObject.type().equals(ClassifierConstraint.type)) {
                ClassifierConstraint classifier = extractMappedObject(mappedConstraint, ClassifierConstraint.class);
                constraint = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints
                        .ClassificationConstraintBuilder().setClassificationConstraint(new ClassificationConstraintBuilder().setClassifier(intentConstraint).build()).build();
            } else {
                continue;
            }

            Constraints intentConstraints = new ConstraintsBuilder().setOrder(order).setConstraints(constraint).build();
            constraintsList.add(intentConstraints);
            order++;
        }

        return constraintsList;
    }
}
