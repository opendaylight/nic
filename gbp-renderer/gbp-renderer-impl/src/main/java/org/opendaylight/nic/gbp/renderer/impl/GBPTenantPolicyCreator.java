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
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.groupbasedpolicy.renderer.ofoverlay.flow.FlowUtils;
import org.opendaylight.groupbasedpolicy.renderer.ofoverlay.sf.AllowAction;
import org.opendaylight.groupbasedpolicy.renderer.ofoverlay.sf.Classifier;
import org.opendaylight.yang.gen.v1.urn.opendaylight.gbp.renderer.rev150511.GbpRendererAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.gbp.renderer.rev150511.GbpRendererAugmentationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.ActionName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.ClassifierName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.ClauseName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.ContractId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.EndpointGroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.L2BridgeDomainId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.L2FloodDomainId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.L3ContextId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.ParameterName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.SelectorName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.SubjectName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.SubnetId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.TargetName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.TenantId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.UniqueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.HasDirection.Direction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.Tenants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.has.action.refs.ActionRefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.has.classifier.refs.ClassifierRefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.subject.feature.instance.ParameterValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.Tenant;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.TenantBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.TenantKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.Contract;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.ContractBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.EndpointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.EndpointGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.EndpointGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.L2BridgeDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.L2FloodDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.L3ContextBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.SubjectFeatureInstancesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.ClauseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.SubjectBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.Target;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.TargetBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.subject.RuleBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.endpoint.group.ConsumerNamedSelectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.endpoint.group.ProviderNamedSelectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.subject.feature.instances.ActionInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.subject.feature.instances.ClassifierInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Conditions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.selector.EndPointSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.selector.EndPointGroupSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class GBPTenantPolicyCreator {

    private static final Logger LOG = LoggerFactory.getLogger(GBPTenantPolicyCreator.class);

    private DataBroker dataProvider;
    private List<Subjects> subjects;
    private Actions intentAction;
    private List<Conditions> conditions;
    private List<Constraints> constraints;

    private final L3ContextId l3c;
    private final L2BridgeDomainId bd;
    private final L2FloodDomainId fd;
    private final SubnetId sub;
    private final SubnetId sub2;
    private final SubnetId sub3;
    private final TenantId tid;
    private final EndpointGroupId eg;
    private final EndpointGroupId eg2;
    private final ContractId cid;

    public GBPTenantPolicyCreator(DataBroker dataBroker,
            List<Subjects> intentSubjects,
            Actions action,
            List<Conditions> intentConditions,
            List<Constraints> intentConstraints) {

        this.dataProvider = dataBroker;
        this.subjects = intentSubjects;
        this.intentAction = action;
        this.conditions = intentConditions;
        this.constraints = intentConstraints;

        this.l3c = new L3ContextId(createUniqueId());
        this.bd = new L2BridgeDomainId(createUniqueId());
        this.fd = new L2FloodDomainId(createUniqueId());
        this.sub = new SubnetId(createUniqueId());
        this.sub2 = new SubnetId(createUniqueId());
        this.sub3 = new SubnetId(createUniqueId());
        this.tid = new TenantId(createUniqueId());
        this.eg = new EndpointGroupId(createUniqueId());
        this.eg2 = new EndpointGroupId(createUniqueId());
        this.cid = new ContractId(createUniqueId());
    }

    public void processIntentToGBP(){
        Tenant tenant = this.getTenant().build();
        this.insertTenant(tenant);

    }

    private TenantBuilder getTenant() {

        EndpointGroup epgConsumer;
        EndpointGroup epgProvider;

        //Prepare all the components of a tenant
        List<EndpointGroup> endpointGroups = this.translateToGBPEndpoints(this.subjects);

        if(endpointGroups !=null && endpointGroups.size() < 2){
            //Create new endpoint groups based on the subjects
            Subjects subjects1 = subjects.get(0);
            Subjects subjects2 = subjects.get(1);

            epgConsumer = new EndpointGroupBuilder()
                .setId(new EndpointGroupId(this.getEndpointIdentifier(subjects1)))
                .setNetworkDomain(sub)
                .setConsumerNamedSelector(ImmutableList.of(new ConsumerNamedSelectorBuilder()
                    .setName(new SelectorName("cns1"))
                    .setContract(ImmutableList.of(cid))
                    .build()))
                .build();


            epgProvider = new EndpointGroupBuilder()
            .setId(new EndpointGroupId(this.getEndpointIdentifier(subjects2)))
            .setNetworkDomain(sub2)
            .setProviderNamedSelector(ImmutableList.of(new ProviderNamedSelectorBuilder()
                .setName(new SelectorName("pns1"))
                .setContract(ImmutableList.of(cid))
                .build()))
            .build();
        }else{
            //Set the properties for the endpoints.
            epgConsumer = endpointGroups.get(0);
            epgProvider = endpointGroups.get(1);
        }

        return new TenantBuilder()
            .setId(tid)
            .setEndpointGroup(ImmutableList.of(epgConsumer, epgProvider))
            .setL3Context(ImmutableList.of(new L3ContextBuilder()
                .setId(l3c)
                .build()))
            .setL2BridgeDomain(ImmutableList.of(new L2BridgeDomainBuilder()
                .setId(bd)
                .setParent(l3c)
                .build()))
            .setL2FloodDomain(ImmutableList.of(new L2FloodDomainBuilder()
                .setId(fd)
                .setParent(bd)
                .build()))
            .setContract(ImmutableList.of(this.getDefaultContract().build()))
            .setSubjectFeatureInstances(new SubjectFeatureInstancesBuilder()
                .setClassifierInstance(ImmutableList.of(
                     new ClassifierInstanceBuilder()
                     .setName(new ClassifierName("ether_type"))
                     .setClassifierDefinitionId(Classifier.ETHER_TYPE_CL.getId())
                     .setParameterValue(ImmutableList.of(new ParameterValueBuilder()
                              .setName(new ParameterName("ethertype"))
                              .setIntValue(Long.valueOf(FlowUtils.IPv4))
                              .build()))
                     .build()))
                .setActionInstance(ImmutableList.of(new ActionInstanceBuilder()
                    .setName(new ActionName("allow"))
                    .setActionDefinitionId(new AllowAction().getId())
                    .build()))
                .build());
    }

    private ContractBuilder getDefaultContract() {
        ContractBuilder contractBuilder = new ContractBuilder().setId(cid);
        Subject subject = getAllowSubject().build();

        if (this.intentAction.getAction() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block)
            subject = this.getBlockSubject().build();

        List<SubjectName> subjectNames = new ArrayList<>();

        subjectNames.add(subject.getName());
        contractBuilder.setSubject(ImmutableList.of(subject));

        return contractBuilder.setClause(ImmutableList.of(new ClauseBuilder().setName(new ClauseName("default_clause"))
            .setSubjectRefs(subjectNames)
            .build()));
    }

    private Subject getSubject(){
        if (this.intentAction.getAction() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow)
            return this.getAllowSubject().build();
        else if (this.intentAction.getAction() instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block)
            return this.getBlockSubject().build();
        else
            return this.getAllowSubject().build();

    }

    private SubjectBuilder getAllowSubject() {
        return new SubjectBuilder()
            .setName(new SubjectName("s1"))
            .setRule(ImmutableList.of(new RuleBuilder()
                .setActionRef(ImmutableList.of(new ActionRefBuilder()
                    .setName(new ActionName("allow"))
                    .build()))
                .setClassifierRef(ImmutableList.of(new ClassifierRefBuilder()
                    .setName(new ClassifierName("ether_type"))
                    .setDirection(Direction.Bidirectional)
                    .build()))
                .build()));
    }

    private SubjectBuilder getBlockSubject() {
        return new SubjectBuilder()
            .setName(new SubjectName("s1"));
    }

    private void insertTenant(final Tenant tenant) {

        final InstanceIdentifier<Tenant> tiid = this.tenantIid(tenant.getId());
        WriteTransaction transaction = dataProvider.newWriteOnlyTransaction();

        transaction.put(LogicalDatastoreType.OPERATIONAL, tiid, tenant, true);
        transaction.submit();
    }

    private Optional<EndpointGroup> readEPGNode(String endpointGroup) {

        ReadWriteTransaction transaction = dataProvider.newReadWriteTransaction();

        Optional<EndpointGroup> node = Optional.absent();

        InstanceIdentifier<EndpointGroup> nodePath = InstanceIdentifier
                .create(Tenants.class)
                .child(Tenant.class)
                .child(EndpointGroup.class,
                        new EndpointGroupKey(
                                buildEndpointGroupId(endpointGroup)));
        try {
            node = transaction.read(LogicalDatastoreType.CONFIGURATION, nodePath)
                    .checkedGet();
        } catch (final ReadFailedException e) {
            LOG.warn("Read Operational/DS for Node fail! {}",
                    nodePath, e);
        }

        return node;
    }

    private List<EndpointGroup> translateToGBPEndpoints(List<Subjects> subjects) {
        List<EndpointGroup> enpointsGroup = Lists.newArrayList();
        String endpointGroupIdentifier = "";

        for (Subjects subs: subjects) {
            endpointGroupIdentifier = this.getEndpointIdentifier(subs);

            //Pull the node from the groupbasedpolicy config datastore
            Optional<EndpointGroup> node = readEPGNode(endpointGroupIdentifier);

            if (node.isPresent()) {
                enpointsGroup.add(node.get());
            }
            else {
                LOG.info("Could not create intent because the EndpointGroup is not defined.");
            }

        }

        return enpointsGroup;
    }

    private String getEndpointIdentifier(Subjects subs){
        String endpointGroupIdentifier = "";

        //Retrieve the appropriate endpoint group identifier
        if (subs.getSubject() instanceof EndPointSelector) {
            EndPointSelector endPointSelector = (EndPointSelector) subs.getSubject();
            endpointGroupIdentifier = endPointSelector.getEndPointSelector();
        }

        if (subs.getSubject() instanceof EndPointGroupSelector) {
            EndPointGroupSelector endPointGroupSelector = (EndPointGroupSelector) subs.getSubject();
            endpointGroupIdentifier = endPointGroupSelector.getEndPointGroupSelector();
        }

        if (subs.getSubject() instanceof EndPointGroup) {
            EndPointGroup endPointGroup = (EndPointGroup) subs.getSubject();
            endpointGroupIdentifier = endPointGroup.getName();
        }

        return endpointGroupIdentifier;
    }

    private EndpointGroupId buildEndpointGroupId(String uuid) {
        EndpointGroupId endpointGroupId = new EndpointGroupId(new UniqueId(uuid));
        return endpointGroupId;
    }

    /**
     * Generate an {@link InstanceIdentifier} for an {@link Tenant}
     * @param tenantKey a tenant key
     * @return the {@link InstanceIdentifier}
     */
    private InstanceIdentifier<Tenant> tenantIid(TenantKey tenantKey) {
        return InstanceIdentifier.builder(Tenants.class)
                .child(Tenant.class, tenantKey)
                .build();
    }

    /**
     * Generate an {@link InstanceIdentifier} for an {@link Tenant}
     * @param tenantId a tenant id
     * @return the {@link InstanceIdentifier}
     */
    private InstanceIdentifier<Tenant> tenantIid(TenantId tenantId) {
        return tenantIid(new TenantKey(tenantId));
    }

    private static String createUniqueId(){
        return java.util.UUID.randomUUID().toString();
    }
}

