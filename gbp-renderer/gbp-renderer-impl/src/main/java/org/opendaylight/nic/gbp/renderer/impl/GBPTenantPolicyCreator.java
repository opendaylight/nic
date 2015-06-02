/*
 * Copyright (c) 2015 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.nic.gbp.renderer.impl;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.groupbasedpolicy.renderer.ofoverlay.sf.AllowAction;
import org.opendaylight.groupbasedpolicy.renderer.ofoverlay.sf.Classifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.ActionName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.ClassifierName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.ClauseName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.ContractId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.EndpointGroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.L2BridgeDomainId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.L2FloodDomainId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.L3ContextId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.NetworkDomainId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.ParameterName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.SelectorName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.SubjectName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.TenantId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.HasDirection.Direction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.has.action.refs.ActionRefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.has.classifier.refs.ClassifierRefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.subject.feature.instance.ParameterValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.Tenant;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.TenantBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.Contract;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.ContractBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.EndpointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.EndpointGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.L2BridgeDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.L2FloodDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.L3ContextBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.SubjectFeatureInstancesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.ClauseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.SubjectBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.subject.RuleBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.endpoint.group.ConsumerNamedSelectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.subject.feature.instances.ActionInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.subject.feature.instances.ClassifierInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.selector.EndPointGroupSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.selector.EndPointSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class GBPTenantPolicyCreator {

    private static final Logger LOG = LoggerFactory.getLogger(GBPTenantPolicyCreator.class);

    private DataBroker dataProvider;
    private MdsalUtils mdsalUtils;

    private Intent intent;

    private static final int  NUM_OF_SUPPORTED_EPG = 2;
    private static final int NUM_OF_SUPPORTED_ACTION = 1;

    // TODO all those need to be either remove or to be retrieve programmatically
    private static final String TENANT_ID = "tenant1";
    private static final String L3CONTEXT_ID = "l3context1";
    private static final String L2BRIDGEDOMAIN_ID = "l2bridgedomain1";
    private static final String L2FLOODDOMAIN_ID = "l2flodddomain1";
    private static final String CLASSIFIER_NAME = "etherType";
    private static final String NETWORKDOMAIN_ID = "networkdomain1";
    private static final String CONTRACT_ID = "contract1";
    private static final String SELECTOR_NAME = "selectorName";
    private static final String SUBJECT_NAME = "s1";
    private static final String ACTION_ALLOW = "allow";

    public GBPTenantPolicyCreator(DataBroker dataBroker,
            Intent intent) {

        this.dataProvider = dataBroker;
        this.intent = intent;

        this.mdsalUtils = new MdsalUtils(dataProvider);

    }

    public void processIntentToGBP(){

        boolean isAcceptable = verifyIntent();
        if (!isAcceptable) {
            return;
        }

        Tenant tenant = this.getTenant().build();
        this.insertTenant(tenant);

    }

    // TODO check constraints, conditions
    private boolean verifyIntent() {
        if (intent.getId() == null) {
            LOG.warn("Intent ID is not specified {}", intent);
            return false;
        }
        if (intent.getActions() == null || intent.getActions().size() > NUM_OF_SUPPORTED_ACTION) {
            LOG.warn("Intent's action is either null or there is more than {} action {}", NUM_OF_SUPPORTED_ACTION, intent);
            return false;
        }
        if (intent.getSubjects() == null || intent.getSubjects().size() > NUM_OF_SUPPORTED_EPG) {
            LOG.warn("Intent's subjects is either null or there is more than {} subjects {}", NUM_OF_SUPPORTED_EPG, intent);
            return false;
        }
        return true;
    }

    private TenantBuilder getTenant() {
        //Prepare all the components of a tenant
        List<EndpointGroup> endpointGroups = this.translateToGBPEndpoints();

        return new TenantBuilder()
            .setId(new TenantId(TENANT_ID))
            .setEndpointGroup(ImmutableList.of(endpointGroups.get(0), endpointGroups.get(1)))
            .setL3Context(ImmutableList.of(new L3ContextBuilder()
                .setId(new L3ContextId(L3CONTEXT_ID))
                .build()))
            .setL2BridgeDomain(ImmutableList.of(new L2BridgeDomainBuilder()
                .setId(new L2BridgeDomainId(L2BRIDGEDOMAIN_ID))
                .setParent(new L3ContextId(L3CONTEXT_ID))
                .build()))
            .setL2FloodDomain(ImmutableList.of(new L2FloodDomainBuilder()
                .setId(new L2FloodDomainId(L2FLOODDOMAIN_ID))
                .setParent(new L2BridgeDomainId(L2BRIDGEDOMAIN_ID))
                .build()))
            .setContract(ImmutableList.of(this.getDefaultContract()))
            .setSubjectFeatureInstances(new SubjectFeatureInstancesBuilder()
                .setClassifierInstance(ImmutableList.of(
                     new ClassifierInstanceBuilder()
                     .setName(new ClassifierName(CLASSIFIER_NAME))
                     .setClassifierDefinitionId(Classifier.ETHER_TYPE_CL.getId())
                     .setParameterValue(ImmutableList.of(new ParameterValueBuilder()
                              .setName(new ParameterName(CLASSIFIER_NAME))
                              .setStringValue("*")
                              .build()))
                     .build()))
                .setActionInstance(ImmutableList.of(new ActionInstanceBuilder()
                    .setName(new ActionName(ACTION_ALLOW))
                    .setActionDefinitionId(new AllowAction().getId())
                    .build()))
                .build());
    }

    private List<EndpointGroup> translateToGBPEndpoints() {
        List<EndpointGroup> endPointGroups = Lists.newArrayList();

        for (Subjects subjects: intent.getSubjects()) {
            String endpointGroupIdentifier = this.getEndpointIdentifier(subjects);

            //Pull the node from the groupbasedpolicy config datastore
            EndpointGroup endPointGroup = readEPGNode(endpointGroupIdentifier);

            if (endPointGroup != null) {
                endPointGroups.add(endPointGroup);
            }
            else {
                // else create the EndPointGroup
                endPointGroups.add(createEndPointGroup(subjects));
            }
        }
        return endPointGroups;
    }

    private String getEndpointIdentifier(Subjects subject){
        String endpointGroupIdentifier = "";

        //Retrieve the appropriate endpointgroup identifier
        if (subject.getSubject() instanceof EndPointSelector) {
            EndPointSelector endPointSelector = (EndPointSelector) subject.getSubject();
            endpointGroupIdentifier = endPointSelector.getEndPointSelector();
        }

        if (subject.getSubject() instanceof EndPointGroupSelector) {
            EndPointGroupSelector endPointGroupSelector = (EndPointGroupSelector) subject.getSubject();
            endpointGroupIdentifier = endPointGroupSelector.getEndPointGroupSelector();
        }

        if (subject.getSubject() instanceof EndPointGroup) {
            EndPointGroup endPointGroup = (EndPointGroup) subject.getSubject();
            endpointGroupIdentifier = endPointGroup.getName();
        }

        return endpointGroupIdentifier;
    }

    private EndpointGroup createEndPointGroup(Subjects subject) {
        return new EndpointGroupBuilder()
                .setId(new EndpointGroupId(this.getEndpointIdentifier(subject)))
                .setNetworkDomain(new NetworkDomainId(NETWORKDOMAIN_ID))
                .setConsumerNamedSelector(ImmutableList.of(new ConsumerNamedSelectorBuilder()
                    .setName(new SelectorName(SELECTOR_NAME))
                    .setContract(ImmutableList.of(new ContractId(CONTRACT_ID)))
                    .build()))
                .build();
    }

    private Contract getDefaultContract() {
        ContractBuilder contractBuilder = new ContractBuilder().setId(new ContractId(CONTRACT_ID));

        Subject subject = null;

        if (intent.getActions().get(0).getAction() instanceof Block) {
            subject = this.getBlockSubject();
        }
        else {
            subject = this.getAllowSubject();
        }

        List<SubjectName> subjectNames = new ArrayList<>();
        subjectNames.add(subject.getName());

        contractBuilder.setSubject(ImmutableList.of(subject));
        contractBuilder.setClause(ImmutableList.of(new ClauseBuilder()
                                                        .setName(new ClauseName("default_clause"))
                                                        .setSubjectRefs(subjectNames)
                                                        .build()));

        return contractBuilder.build();
    }

    private Subject getAllowSubject() {
        return new SubjectBuilder()
            .setName(new SubjectName(SUBJECT_NAME))
            .setRule(ImmutableList.of(new RuleBuilder()
                .setActionRef(ImmutableList.of(new ActionRefBuilder()
                    .setName(new ActionName(ACTION_ALLOW))
                    .build()))
                .setClassifierRef(ImmutableList.of(new ClassifierRefBuilder()
                    .setName(new ClassifierName(CLASSIFIER_NAME))
                    .setDirection(Direction.Bidirectional)
                    .build()))
                .build()))
             .build();
    }

    private Subject getBlockSubject() {
        return new SubjectBuilder()
            .setName(new SubjectName(SUBJECT_NAME))
            .build();
    }

    private void insertTenant(Tenant tenant) {
        InstanceIdentifier<Tenant> tenantInstanceIdentifier = GBPRendererHelper.createTenantIid(tenant.getId());
        mdsalUtils.put(LogicalDatastoreType.OPERATIONAL, tenantInstanceIdentifier, tenant);
    }

    private EndpointGroup readEPGNode(String endpointGroup) {
        EndpointGroupId endPointGroupId = new EndpointGroupId(endpointGroup);
        InstanceIdentifier<EndpointGroup> nodePath = GBPRendererHelper.createEndPointGroupIid(endPointGroupId);
        return mdsalUtils.read(LogicalDatastoreType.CONFIGURATION, nodePath);
    }
}

