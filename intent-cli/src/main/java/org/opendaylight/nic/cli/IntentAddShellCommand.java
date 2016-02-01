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
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.classification.constraint.ClassificationConstraintBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.qos.constraint.QosConstraintBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

import java.io.InvalidObjectException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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

    // Enter an empty Constraints field.
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
    String profilename = "";

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

    private String buildActionNotFoundMessage(String action) {
        if(action == null || action.isEmpty()) {
            action = "(empty)";
        }

        return "No subject found with value: " + action + " in the mapping service. Try adding it using: intent:map' command";
    }

    protected List<Actions> createActions() throws InvalidObjectException {
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
                //TODO Bug 3956, 4155 Return message "add action in intent:map"
                throw new InvalidObjectException(buildActionNotFoundMessage(intentAction));
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

    //TODO Move these checks to Util
    private boolean checkMAC (String mac) {
        if (mac == null || mac.isEmpty()) return false;
        Pattern macPattern = Pattern.compile("([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}");
        Matcher macMatcher = macPattern.matcher(mac);
        return macMatcher.matches();
    }

    private boolean checkIP (String ip) {
        if (ip == null || ip.isEmpty()) return false;
        ip = ip.trim();
        if ((ip.length() < 6) & (ip.length() > 15)) return false;

        try {
            Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
            Matcher matcher = pattern.matcher(ip);
            return matcher.matches();
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }

    protected List<Subjects> createSubjects() throws InvalidObjectException {
        List<Subjects> subjectList = new ArrayList<Subjects>();

        if (!checkIP(this.from) && !checkIP(this.to) && !checkMAC(this.to) && !checkMAC(this.from)) {
            if(mappingService.get(this.from).isEmpty())
                throw new InvalidObjectException(buildSubjectNotFoundMessage(this.from));

            if(mappingService.get(this.to).isEmpty())
                throw new InvalidObjectException(buildSubjectNotFoundMessage(this.to));

            //Case when from and to are labels, check whether value is IP or MAC
            Map<String, String> subjectFromMap = mappingService.get(this.from);
            if (checkIP(subjectFromMap.get("IP"))) {
                this.from = subjectFromMap.get("IP");
            } else if (checkMAC(subjectFromMap.get("MAC"))) {
                this.from = subjectFromMap.get("MAC");
            }

            Map<String, String> subjectToMap = mappingService.get(this.to);
            if (checkIP(subjectFromMap.get("IP"))) {
                this.to = subjectToMap.get("IP");
            } else if (checkMAC(subjectFromMap.get("MAC"))) {
                this.to = subjectToMap.get("MAC");
            }
            // TODO if attributes are not there then display message for attributes.
        }

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
    protected List<Constraints> createConstraints() throws InvalidObjectException {
        final List<Constraints> constraintsList = new ArrayList<Constraints>();
        short order = 1;
        // Default entry to constraints for QOS
        Gson gson = new Gson();
        MappedObject defaultQoS = new QualityOfServiceConstraint("DEFAULT");
        defaultQoS.setKey("DEFAULT");
        String object = gson.toJson(defaultQoS, MappedObject.class);

        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put(NicProvider.CONSTRAINT_QOS, object);
        mappingService.add(NicProvider.CONSTRAINT_QOS, defaultMap);

        //Check the list for new constraints
        for (String intentConstraint : this.constraints) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.Constraints constraint = null;

            Map<String, String> mappedConstraint = mappingService.get(intentConstraint);

            if(mappedConstraint.isEmpty())
                throw new InvalidObjectException(buildConstraintNotFoundMessage(intentConstraint));

            MappedObject mappedObject = null;
            if((mappedObject = MappedObject.extractFirstMappedObject(mappedConstraint, MappedObject.class)) == null)
                throw new InvalidObjectException(buildConstraintNotFoundMessage(intentConstraint));

            if (mappedObject.type().equals(QualityOfServiceConstraint.TYPE)) {

                QualityOfServiceConstraint qos = QualityOfServiceConstraint.fromMappedObject(mappedObject);
                this.profilename = (this.profilename.isEmpty()) ? qos.getProfileName() : this.profilename;

                constraint = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints
                        .QosConstraintBuilder().setQosConstraint(new QosConstraintBuilder().setQosName(this.profilename).build()).build();
            } else if (mappedObject.type().equals(ClassifierConstraint.TYPE)) {
                ClassifierConstraint classifier = ClassifierConstraint.fromMappedObject(mappedObject);
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
