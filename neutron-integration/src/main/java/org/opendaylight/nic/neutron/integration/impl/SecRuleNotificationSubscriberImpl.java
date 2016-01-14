/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.neutron.integration.impl;

import com.google.common.base.Optional;
import com.google.gson.Gson;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.listeners.api.*;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.ActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.allow.AllowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.ConditionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.ConstraintsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.IntentsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.SubjectsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.ClassificationConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.ClassificationConstraintBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.nic.neutron.NeutronSecurityRule;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.constants.rev150712.DirectionEgress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.constants.rev150712.DirectionIngress;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SecRuleNotificationSubscriberImpl implements IEventListener<NicNotification> {

    private NeutronSecurityRule securityRule;
    private static final int FIRST_SUBJECT = 1;
    private static final int SECOND_SUBJECT = 2;
    public static final InstanceIdentifier<Intents> INTENTS_IID = InstanceIdentifier.builder(Intents.class).build();
    private static final Logger LOG = LoggerFactory.getLogger(SecRuleNotificationSubscriberImpl.class);
    private DataBroker dataBroker;
    private Map<String, org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid> ruleToIntentMap;

    public SecRuleNotificationSubscriberImpl(DataBroker db) {
        this.dataBroker = db;
        this.ruleToIntentMap = new HashMap<>();
    }

    @Override
    public void handleEvent(NicNotification event) {
        if (SecurityRuleAdded.class.isInstance(event)) {
            //Translate Security rules into intents
            SecurityRuleAdded securityRuleAdded = (SecurityRuleAdded) event;
            securityRule = securityRuleAdded.getSecurityRule();
            Intent intent = createIntent();
            ruleToIntentMap.put(securityRule.getSecurityRuleID(), intent.getId());
            addIntentToMDSAL(intent, FlowAction.ADD_FLOW);
        }
        else if (SecurityRuleDeleted.class.isInstance(event)) {
            SecurityRuleDeleted securityRuleDeleted = (SecurityRuleDeleted) event;
            securityRule = (NeutronSecurityRule) securityRuleDeleted.getSecurityRule();
            Uuid uuid = ruleToIntentMap.get(securityRule.getSecurityRuleID());
            removeIntent(uuid);
        }
        else if(SecurityRuleUpdated.class.isInstance(event)) {
            //TODO:
        }
    }

    private void addIntentToMDSAL(Intent intent, FlowAction flowAction) {
        Intents intents;
        List<Intent> listOfIntents = listIntents(true);

        try {
            listOfIntents.add(intent);
            intents = new IntentsBuilder().setIntent(listOfIntents).build();

            // Place default config data in data store tree
            WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
            tx.put(LogicalDatastoreType.CONFIGURATION, INTENTS_IID, intents);
            // Perform the tx.submit synchronously
            tx.submit();
        } catch (Exception e) {
            LOG.error("addIntent: failed: {}", e);
        }
    }

    //TODO: Move to utils folder as code repetition in NicProvider
    public void removeIntent(Uuid id) {
        try {
            InstanceIdentifier<Intent> iid = InstanceIdentifier.create(Intents.class)
                .child(Intent.class, new IntentKey(id));
            // Removes default config data in data store tree
            WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
            tx.delete(LogicalDatastoreType.CONFIGURATION, iid);
            // Perform the tx.submit synchronously
            tx.submit();
        } catch (Exception e) {
            LOG.info("RemoveIntent: failed: {}", e);
        }
    }

    //TODO: Move to utils folder as code repetition in NicProvider
    public List<Intent> listIntents(boolean isConfigurationDatastore) {
        List<Intent> listOfIntents = null;

        try {
            ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
            Optional<Intents> intents = tx.read((isConfigurationDatastore) ? LogicalDatastoreType.CONFIGURATION
                : LogicalDatastoreType.OPERATIONAL, INTENTS_IID).checkedGet();

            if(intents.isPresent()) {
                listOfIntents = intents.get().getIntent();
            }
            else {
                LOG.info("Intent tree was empty!");
            }
        } catch (Exception e) {
            LOG.error("ListIntents: failed: {}", e.getMessage(), e);
        }

        if (listOfIntents == null) {
            listOfIntents = new ArrayList<Intent>();
        }
        return listOfIntents;
    }

    /**
     * Converts the security rule to intent with the ports, protocols and EtherType as
     * a constraint
     */
    private Intent createIntent() {
        String fromString = "any";
        String toString = "any";
        IntentBuilder intentBuilder = new IntentBuilder();
        UUID uuid = UUID.randomUUID();

        intentBuilder. setId(new Uuid(uuid.toString()));

        if (securityRule.getSecurityRuleDirection().compareTo("DirectionEgress") == 0) {
            if (securityRule.getSecurityRuleRemoteIpPrefix() != null) {
                toString = securityRule.getSecurityRuleRemoteIpPrefix();
                if(toString.contains("0.0.0.0")) {
                    toString = "any";
                }
            }
        }
        else if (securityRule.getSecurityRuleDirection().compareTo("DirectionIngress") == 0) {
            if (securityRule.getSecurityRuleRemoteIpPrefix() != null) {
                fromString = securityRule.getSecurityRuleRemoteIpPrefix();
                if(fromString.contains("0.0.0.0")) {
                    fromString = "any";
                }
            }
        }
        intentBuilder.setActions(Arrays.asList(
            new ActionsBuilder().setOrder((short) 1).setAction(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action
                    .AllowBuilder().setAllow(new AllowBuilder().build()).build()).build()));

        Subjects to = subject((short) SECOND_SUBJECT, toString);
        Subjects from = subject((short) FIRST_SUBJECT, fromString);
        intentBuilder.setSubjects(Arrays.asList(from, to));

        ClassificationConstraint constraint = classificationConstraint();
        intentBuilder.setConstraints(Arrays.asList(new ConstraintsBuilder().setOrder((short) 1).setConstraints(constraint).build()));

        intentBuilder. setId(new Uuid(uuid.toString()));

        return intentBuilder.build();
    }

    private Subjects subject(short order, String name) {
        return new SubjectsBuilder()
            .setSubject(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder()
                    .setEndPointGroup(
                        new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroupBuilder()
                            .setName(name).build()).build()).setOrder(order).build();
    }

    private ClassificationConstraint classificationConstraint() {
        //use gson to convert Neutron Security object to json, then add to constraint
        Gson gson = new Gson();
        String portObject = gson.toJson(securityRule);

        return new ClassificationConstraintBuilder()
            .setClassificationConstraint(new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.classification.constraint.ClassificationConstraintBuilder()
                .setClassifier(portObject).build()).build();
    }

}