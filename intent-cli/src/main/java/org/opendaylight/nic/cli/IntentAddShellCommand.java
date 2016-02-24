/*
 * Copyright (c) 2015 Ciena and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.cli;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.nic.api.NicConsoleProvider;
import org.opendaylight.nic.constraints.ClassifierConstraint;
import org.opendaylight.nic.constraints.QualityOfServiceConstraint;
import org.opendaylight.nic.impl.NicProvider;
import org.opendaylight.nic.mapped.MappedObject;
import org.opendaylight.nic.mapping.api.IntentMappingService;
import org.opendaylight.nic.utils.IntentUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(IntentAddShellCommand.class);
    private static final int FIRST_SUBJECT = 1;
    private static final int SECOND_SUBJECT = 2;
    private static final String ANY = "any";
    private static final String IP = "IP";
    private static final String MAC = "MAC";
    private static final String DEFAULT = "LOW";

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
            LOG.error("Error at executing intent attributes creation");
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
        LOG.warn("ACTION NOT FOUND");
        return "No action found with: " + action + " in this release";
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
                //Bug 3956, 4155 Return message "add action in intent:map"
                LOG.error("No action found with: " + intentAction + " in this release");
                return  null;
                //throw new InvalidObjectException(buildActionNotFoundMessage(intentAction));
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
        LOG.warn("SUBJECT NOT FOUND");
        return "No subject found with value: " + subject + " in the mapping service. Try adding it using: intent:map' command";
    }

    private String buildConstraintNotFoundMessage(String constraint) {
        if(constraint == null || constraint.isEmpty()) {
            constraint = "(empty)";
        }
        LOG.warn("CONSTRAINT NOT FOUND");
        return "No constraint found with value: " + constraint + " in the mapping service. Try adding it using: intent:map' command";
    }

    protected List<Subjects> createSubjects() throws InvalidObjectException {
        List<Subjects> subjectList = new ArrayList<Subjects>();

        if (!IntentUtils.validateIP(this.from) && !IntentUtils.validateIP(this.to)
                && !IntentUtils.validateMAC(this.to) && !IntentUtils.validateMAC(this.from)) {
            LOG.info("The subjects are not IP or MAC addresses");
            Map<String, String> subjectFromMap = mappingService.get(this.from);
            Map<String, String> subjectToMap = mappingService.get(this.to);
            if(subjectFromMap.isEmpty()) {
                throw new InvalidObjectException(buildSubjectNotFoundMessage(this.from));
            }

            if(subjectToMap.isEmpty()) {
                throw new InvalidObjectException(buildSubjectNotFoundMessage(this.to));
            }

            //Case when from and to are labels, check whether value is IP or MAC
            if (IntentUtils.validateIP(subjectFromMap.get(IP))) {
                this.from = subjectFromMap.get(IP);
            } else if (IntentUtils.validateMAC(subjectFromMap.get(MAC))) {
                this.from = subjectFromMap.get(MAC);
            }

            if (IntentUtils.validateIP(subjectToMap.get(IP))) {
                this.to = subjectToMap.get(IP);
            } else if (IntentUtils.validateMAC(subjectToMap.get(MAC))) {
                this.to = subjectToMap.get(MAC);
            }
            // TODO map the subjects to mapping services mapped-objects
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
     * Method to input default entry to QoS
     */
    void defaultQoSEntry () {
        // Default entry to constraints for QOS
        Gson gson = new Gson();
        MappedObject defaultQoS = new QualityOfServiceConstraint(DEFAULT);
        defaultQoS.setKey(DEFAULT);
        String object = gson.toJson(defaultQoS, MappedObject.class);

        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put(NicProvider.CONSTRAINT_QOS, object);
        mappingService.add(NicProvider.CONSTRAINT_QOS, defaultMap);
    }

    /**
     * Returns the list of Constraints.
     */
    protected List<Constraints> createConstraints() throws InvalidObjectException {
        final List<Constraints> constraintsList = new ArrayList<Constraints>();
        short order = 1;

        //Check the list for new constraints
        for (String intentConstraint : this.constraints) {
            org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.Constraints constraint = null;
            MappedObject mappedObject = null;

            if (this.profilename.isEmpty()) {
                this.profilename = null;
            } else {
                Map<String, String> mappedConstraint = mappingService.get(intentConstraint);

                if(mappedConstraint.isEmpty()) {
                    throw new InvalidObjectException(buildConstraintNotFoundMessage(intentConstraint));
                }

                try {
                    if ((mappedObject = MappedObject.extractFirstMappedObject(mappedConstraint, MappedObject.class)) == null) {
                        throw new InvalidObjectException(buildConstraintNotFoundMessage(intentConstraint));
                    }
                } catch (JsonSyntaxException exception) {
                    LOG.error("JSON format unknown, unable to parse");
                    throw new InvalidObjectException("Unknown JSON format!");
                }
                // FIXME Mapped Object for QoS is not pushing flows
                QualityOfServiceConstraint qos = QualityOfServiceConstraint.fromMappedObject(mappedObject);
                this.profilename = qos.getProfileName();
            }

            if (intentConstraint.equalsIgnoreCase(NicProvider.CONSTRAINT_QOS)) {
                LOG.info("QoS Constraint match");

                constraint = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints
                        .QosConstraintBuilder().setQosConstraint(new QosConstraintBuilder().setQosName(this.profilename).build()).build();
            }  else if (intentConstraint.equalsIgnoreCase(NicProvider.CONSTRAINT_CLASSIFIER)) {
                LOG.info("Classifier Constraint match");
                if (mappedObject.type().equals(ClassifierConstraint.TYPE)) {
                    ClassifierConstraint classifier = ClassifierConstraint.fromMappedObject(mappedObject);
                }
                constraint = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints
                        .ClassificationConstraintBuilder().setClassificationConstraint(new ClassificationConstraintBuilder().setClassifier(intentConstraint).build()).build();
            }
            else {
                LOG.info("No Constraint match");
                continue;
                //throw new InvalidObjectException(buildConstraintNotFoundMessage(intentConstraint));
            }

            Constraints intentConstraints = new ConstraintsBuilder().setOrder(order).setConstraints(constraint).build();
            constraintsList.add(intentConstraints);
            order++;
        }

        return constraintsList;
    }
}
