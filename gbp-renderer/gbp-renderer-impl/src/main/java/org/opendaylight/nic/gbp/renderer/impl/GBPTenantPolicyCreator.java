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
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.ContextId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.ContractId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.EndpointGroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.L2BridgeDomainId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.L2FloodDomainId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.L3ContextId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.NetworkDomainId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.ParameterName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.RuleName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.SelectorName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.SubjectName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.SubnetId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.TenantId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.endpoint.rev140421.Endpoints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.endpoint.rev140421.endpoint.fields.L3Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.endpoint.rev140421.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.HasDirection.Direction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.has.action.refs.ActionRefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.has.classifier.refs.ClassifierRefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.has.classifier.refs.ClassifierRefKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.subject.feature.instance.ParameterValueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.Tenant;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.TenantBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.Contract;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.ContractBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.EndpointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.EndpointGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.L2BridgeDomain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.L2BridgeDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.L2FloodDomain;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.L2FloodDomainBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.L3Context;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.L3ContextBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.SubjectFeatureInstancesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.Subnet;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.SubnetBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.ClauseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.SubjectBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.subject.RuleBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.subject.RuleKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.endpoint.group.ConsumerNamedSelectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.endpoint.group.ProviderNamedSelectorBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.subject.feature.instances.ActionInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.subject.feature.instances.ClassifierInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * Group based Policy tenant policy creator takes an intent and
 * converts it to a tenant policy which then gets pushed into the config datastore
 * for the groupbasedpolicy rendering to the appropriate network devices.
 *
 * Assumptions:
 *
 * 1) At this point, we assume only two subjects for each intent. The implication
 *      is that even though GBP can support multiple subnets, we will only support two.
 *      One for the service provider and the other for the service consumer.
 *
 * 2) The subject names in intent must be the same as the associated endpoint "endpoint-group"
 *      attribute
 *
 */
public class GBPTenantPolicyCreator {

    private static final Logger LOG = LoggerFactory.getLogger(GBPTenantPolicyCreator.class);

    private DataBroker dataProvider;
    private MdsalUtils mdsalUtils;

    private static final int  NUM_OF_SUPPORTED_EPG = 2;
    private static final int NUM_OF_SUPPORTED_ACTION = 1;

    private static final String CLASSIFIER_NAME = "etherType";
    private static final String SUBJECT_NAME = "s1";
    private static final String ACTION_ALLOW = "allow";
    private static final String PROVIDER_NETWORK_NAME="pns1";
    private static final String CONSUMER_NETWORK_NAME="cns1";
    private static final String DEFAULT_CONTRACT="default-nic-contract";

    //Attributes of the tenant policy
    private List<L3Address> providerL3Addresses;
    private List<L3Address> consumerL3Addresses;
    private NetworkDomainId providerNetworkDomainId;
    private NetworkDomainId consumerNetworkDomainId;
    private L2FloodDomainId providerFloodDomainId;
    private L2FloodDomainId consumerFloodDomainId;
    private TenantId tenantId;
    private List<L2BridgeDomainId> bridgeDomainIds;
    private List<L3ContextId> l3ContextIds;
    private String contractId;
    private Intent intent;

    public GBPTenantPolicyCreator(DataBroker dataBroker,
            Intent intent) {
        this.dataProvider = dataBroker;
        this.intent = intent;
        this.mdsalUtils = new MdsalUtils(dataProvider);
        this.contractId = GBPRendererHelper.createUniqueId();
        this.providerL3Addresses = new ArrayList<>();
        this.consumerL3Addresses = new ArrayList<>();
        this.bridgeDomainIds = new ArrayList<>();
        this.l3ContextIds = new ArrayList<>();

        this.providerNetworkDomainId = new NetworkDomainId(GBPRendererHelper.createUniqueId());
        this.consumerNetworkDomainId = new NetworkDomainId(GBPRendererHelper.createUniqueId());
        this.providerFloodDomainId = new L2FloodDomainId(GBPRendererHelper.createUniqueId());
        this.consumerFloodDomainId = new L2FloodDomainId(GBPRendererHelper.createUniqueId());
    }

    public void processIntentToGBP(){

        if(!this.verifyIntent()){
            return;
        }

        TenantBuilder tenantBuilder = this.getTenant();

        if(tenantBuilder == null) {
            return;
        }
        else{
            Tenant tenant = tenantBuilder.build();

            //TODO - Add modules to support tenant update and delete
            InstanceIdentifier<Tenant> tenantInstanceIdentifier = GBPRendererHelper.createTenantIid(tenant.getId());
            mdsalUtils.put(LogicalDatastoreType.CONFIGURATION, tenantInstanceIdentifier, tenant);
            LOG.info("Policy tenant successfully inserted into the config store");
        }
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

        //Prepare data for the tenant
        List<L3Context> l3Contexts = new ArrayList<>();
        List<L2BridgeDomain> l2BridgeDomains = new ArrayList<>();
        List<L2FloodDomain> l2FloodDomains = new ArrayList<>();


        LOG.info("Retrieving intent properties from GBP endpoints");
        //Prepare all the components of a tenant
        List<EndpointGroup> endpointGroups = this.createEndpointGroups();

        if(endpointGroups == null || endpointGroups.size() < NUM_OF_SUPPORTED_EPG){
            LOG.error("Tenant policy creation failed because of two few endpoint groups");
            return null;
        }


        //Most endpoint definitions will present only one l3context
        for(L3ContextId contextId: l3ContextIds){
            l3Contexts.add(new L3ContextBuilder()
                .setId(contextId).build());
        }

        //Most endpoint definitions will present only one bridge domain
        for(L2BridgeDomainId bridgeDomainId : this.bridgeDomainIds){
            l2BridgeDomains.add(
                    new L2BridgeDomainBuilder()
                    .setId(bridgeDomainId)
                    .setParent(l3ContextIds.get(0))
                    .build());

            l2FloodDomains.add(new L2FloodDomainBuilder()
                .setId(this.consumerFloodDomainId)
                .setParent(bridgeDomainId)
                .build());

            l2FloodDomains.add(new L2FloodDomainBuilder()
            .setId(this.providerFloodDomainId)
            .setParent(bridgeDomainId)
            .build());
        }

        return new TenantBuilder()
            .setId(this.tenantId)
            .setEndpointGroup(ImmutableList.copyOf(endpointGroups))
            .setL3Context(ImmutableList.copyOf(l3Contexts))
            .setL2BridgeDomain(ImmutableList.copyOf(l2BridgeDomains))
            .setL2FloodDomain(ImmutableList.copyOf(l2FloodDomains))
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
                .build())
                .setSubnet(ImmutableList.of(
                        this.createSubnet(consumerL3Addresses, consumerNetworkDomainId, consumerFloodDomainId)
                        , this.createSubnet(providerL3Addresses, providerNetworkDomainId, providerFloodDomainId)));
    }

    private String getEndpointIdentifier(Subjects subject){
        String endpointGroupIdentifier = "";

        //Retrieve the appropriate endpoint group identifier
        if (subject.getSubject() instanceof EndPointSelector) {
            EndPointSelector endPointSelector = (EndPointSelector) subject.getSubject();
            endpointGroupIdentifier = endPointSelector.getEndPointSelector().getEndPointSelector();
        }

        if (subject.getSubject() instanceof EndPointGroupSelector) {
            EndPointGroupSelector endPointGroupSelector = (EndPointGroupSelector) subject.getSubject();
            endpointGroupIdentifier = endPointGroupSelector.getEndPointGroupSelector().getEndPointGroupSelector();
        }

        if (subject.getSubject() instanceof EndPointGroup) {
            EndPointGroup endPointGroup = (EndPointGroup) subject.getSubject();
            endpointGroupIdentifier = endPointGroup.getEndPointGroup().getName();
        }

        return endpointGroupIdentifier;
    }

    private Contract getDefaultContract() {
        ContractBuilder contractBuilder = new ContractBuilder().setId(new ContractId(this.contractId));

        Subject subject = null;
        Action action = intent.getActions().get(0).getAction();
        if (action instanceof Block) {
            subject = this.getBlockSubject();
        }
        else if (action instanceof Allow) {
            subject = this.getAllowSubject();
        }
        else {
            LOG.warn("The specified action is not recognized {}", action);
        }

        List<SubjectName> subjectNames = new ArrayList<>();
        subjectNames.add(subject.getName());

        contractBuilder.setSubject(ImmutableList.of(subject));
        contractBuilder.setClause(ImmutableList.of(new ClauseBuilder()
                                                        .setName(new ClauseName(DEFAULT_CONTRACT))
                                                        .setSubjectRefs(subjectNames)
                                                        .build()));

        return contractBuilder.build();
    }

    private Subject getAllowSubject() {
        return new SubjectBuilder()
            .setName(new SubjectName(SUBJECT_NAME))
            .setRule(ImmutableList.of(new RuleBuilder()
                .setKey(new RuleKey(new RuleName(SUBJECT_NAME + "_Rule")))
                .setActionRef(ImmutableList.of(new ActionRefBuilder()
                    .setName(new ActionName(ACTION_ALLOW))
                    .build()))
                .setClassifierRef(ImmutableList.of(new ClassifierRefBuilder()
                    .setKey(new ClassifierRefKey(new ClassifierName(SUBJECT_NAME + "_Classifier")))
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

    private List<EndpointGroup> createEndpointGroups(){
        EndpointGroup epgConsumer;
        EndpointGroup epgProvider;
        List<EndpointGroup> endpointGroups = new ArrayList<>();


        if(intent.getSubjects().size() == NUM_OF_SUPPORTED_EPG){
            //Create new endpoint groups based on the subjects
            List<Subjects> subjects = intent.getSubjects();

            Subjects subjects1 = subjects.get(0);
            Subjects subjects2 = subjects.get(1);

            //If we do not find matching endpoints to the consumer subject, we are done
            if(!this.getTenantEndpointAttributes(this.getEndpointIdentifier(subjects1), false)){
                return null;
            }

            epgConsumer = new EndpointGroupBuilder()
                .setId(new EndpointGroupId(this.getEndpointIdentifier(subjects1)))
                .setNetworkDomain(consumerNetworkDomainId)
                .setConsumerNamedSelector(ImmutableList.of(new ConsumerNamedSelectorBuilder()
                    .setName(new SelectorName(CONSUMER_NETWORK_NAME))
                    .setContract(ImmutableList.of(new ContractId(this.contractId)))
                    .build()))
                .build();


          //If we do not find matching endpoints for the provider subject, we are done
            if(!this.getTenantEndpointAttributes(this.getEndpointIdentifier(subjects2), true)){
                return null;
            }

            epgProvider = new EndpointGroupBuilder()
            .setId(new EndpointGroupId(this.getEndpointIdentifier(subjects2)))
            .setNetworkDomain(providerNetworkDomainId)
            .setProviderNamedSelector(ImmutableList.of(new ProviderNamedSelectorBuilder()
                .setName(new SelectorName(PROVIDER_NETWORK_NAME))
                .setContract(ImmutableList.of(new ContractId(this.contractId)))
                .build()))
            .build();

            endpointGroups.add(epgConsumer);
            endpointGroups.add(epgProvider);

            return endpointGroups;

        }

        return null;
    }

    private Subnet createSubnet(List<L3Address> endPoints, NetworkDomainId networkDomainId, L2FloodDomainId floodDomainId){
        L3Address endPoint = endPoints.get(0);
        String ip = endPoint.getIpAddress().getIpv4Address().getValue();
        ip = ip.substring(0, ip.lastIndexOf('.')) + ".1";

        return new SubnetBuilder()
            .setId(new SubnetId(networkDomainId.getValue()))
            .setParent(new ContextId(floodDomainId.getValue()))
            .setVirtualRouterIp(
                GBPRendererHelper.createIpAddress(ip))
             .setIpPrefix(GBPRendererHelper.createIpPrefix(ip + "/24"))
            .build();
    }

    /**
     * Gets network details from the endpoint registry
     * @param subjectId
     *                  Unique identifier to the endpoint group
     * @param provider
     *                  If set to TRUE, associate the subject and endpoints with the provider ELSE associate with the consumer.
     * @return Returns
     *                  TRUE if there are endpoints associated with this subject
     */
    private Boolean getTenantEndpointAttributes(String subjectId, Boolean provider){

        List<Endpoint> matchingEndpoints = this.readEPNodes(subjectId);
        LOG.info("Matching endpoints for {}: {}",subjectId, matchingEndpoints.size());

        //Pull details from one of the endpoints
        if(matchingEndpoints == null || matchingEndpoints.size() == 0){
            LOG.error("Subject id: {} has no matching endpoints", subjectId);
            return false;
        }

        for(Endpoint e: matchingEndpoints){

            if(this.tenantId == null){
                this.tenantId = e.getTenant();
            }

            if(!GBPRendererHelper.contains(this.bridgeDomainIds,  e.getL2Context())){
                this.bridgeDomainIds.add(e.getL2Context());
            }

            for(L3Address address : e.getL3Address()){
                LOG.info("{} address: {}"
                        , provider ? "Provider" : "Consumer"
                        , GBPRendererHelper.getStringIpAddress(address.getIpAddress())
                        );

                if(provider){
                    if(!GBPRendererHelper.contains(providerL3Addresses, address)){
                        providerL3Addresses.add(address);
                    }
                }else{
                    if(!GBPRendererHelper.contains(consumerL3Addresses, address)){
                        consumerL3Addresses.add(address);
                    }
                }

                if(!GBPRendererHelper.contains(this.l3ContextIds, address.getL3Context())){
                        this.l3ContextIds.add(address.getL3Context());
                }
            }
        }

        LOG.info("L3 Address for {}: {}"
                , subjectId
                , provider? providerL3Addresses.size(): consumerL3Addresses.size()
        );

        return true;
    }


    /***
     * Gets the list of endpoints that matches an intent subject id
     * @param subjectId
     * @return
     */
    private List<Endpoint> readEPNodes(String subjectId){
        List<Endpoint> matchingGroup = new ArrayList<>();

        InstanceIdentifier<Endpoints> nodePath = GBPRendererHelper.createEndpointsIdentifier();
        Endpoints node = mdsalUtils.read(LogicalDatastoreType.OPERATIONAL, nodePath);

        if (node != null) {
            for(Endpoint e : node.getEndpoint()){
                if(e.getEndpointGroup().getValue().equalsIgnoreCase(subjectId)){
                    matchingGroup.add(e);
                }
            }
        }else{
            LOG.error("Endpoints not found for the path {}", nodePath);
        }

        return matchingGroup;
    }
}
