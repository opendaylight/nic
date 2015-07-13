/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.gbp.renderer.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.EndpointGroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.L2BridgeDomainId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.L2FloodDomainId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.L3ContextId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.NetworkDomainId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.TenantId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.endpoint.rev140421.endpoint.fields.L3Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.endpoint.rev140421.endpoint.fields.L3AddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.endpoint.rev140421.Endpoints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.endpoint.rev140421.EndpointsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.endpoint.rev140421.endpoints.Endpoint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.endpoint.rev140421.endpoints.EndpointBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.ActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.AllowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.BlockBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.SubjectsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * JUnit test for {@link GBPTenantPolicyCreator}.
 *
 * GBPTenantPolicyCreator test class is to test Group based Policy tenant policy creator takes an intent and
 * converts it to a tenant policy which then gets pushed into the config datastore
 * for the groupbasedpolicy rendering to the appropriate network devices.
 */
@RunWith(PowerMockRunner.class)
public class GBPTenantPolicyCreatorTest {

    /**
     * Mock object for DataBroker.
     */
    @Mock private DataBroker mockBroker;
    /**
     * Mock object for ReadOnlyTransaction.
     */
    @Mock private ReadOnlyTransaction mockReadTransaction;
    /**
     * Mock object for WriteTransaction.
     */
    @Mock private WriteTransaction mockWriteTransaction;
    /**
     * Mock object for Optional.
     */
    @Mock private Optional<Endpoints> mockOptionalDataObject;
    /**
     * Mock object for CheckedFuture.
     */
    @Mock private CheckedFuture<Optional<Endpoints>, ReadFailedException> mockFuture;
    /**
     * Mock object for CheckedFuture.
     */
    @Mock private CheckedFuture<Void, TransactionCommitFailedException> mockCheckedFuture;
    /**
     * String declaration for UniqueId.
     */
    private static final String UNIQUE_ID = "891fc7a8-cca7-45ee-9128-3294b96307d0";
    /**
     * IntentKey object reference.
     */
    private IntentKey intentKey;
    /**
     * String declaration for IP Address.
     */
    private static final String TEST_IP = "192.168.194.131";
    /**
     * Allow object reference.
     */
    private Allow allow;
    /**
     * Block object reference.
     */
    private Block block;
    /**
     * Actions object reference for Allow action.
     */
    private Actions actionAllow;
    /**
     * Actions object reference for Block action.
     */
    private Actions actionBlock;
    /**
     * Subjects object reference for source.
     */
    private Subjects srcsubject;
    /**
     * Subjects object reference for destination.
     */
    private Subjects destsubject;
    /**
     * Declare list of Actions.
     */
    final List<Actions> actionlist = new ArrayList<Actions>();
    /**
     * Declare list of subjects.
     */
    private List<Subjects> subjectslist;
    /**
     * Declare list of L3Address.
     */
    private List<L3Address> l3List;
    /**
     * GBPTenantPolicyCreator object to perform unit testing.
     */
    private GBPTenantPolicyCreator gbpTenantPolicyCreator;

    /**
     * This method creates the required objects to perform unit testing.
     */
    @Before
    public void setUp() throws Exception {
        intentKey = new IntentKey(new Uuid(UNIQUE_ID));
        allow = new AllowBuilder().build();
        block = new BlockBuilder().build();
        actionAllow = new ActionsBuilder().setAction(allow).build();
        actionBlock = new ActionsBuilder().setAction(block).build();
        actionlist.add(actionAllow);
        EndPointGroup src = new EndPointGroupBuilder().setName(UNIQUE_ID).build();
        EndPointGroup dest = new EndPointGroupBuilder().setName(UNIQUE_ID).build();
        Subject srcAddress = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder().setEndPointGroup(src).build();
        Subject destAddress = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder().setEndPointGroup(dest).build();
        srcsubject = new SubjectsBuilder().setSubject(srcAddress).build();
        destsubject = new SubjectsBuilder().setSubject(destAddress).build();
        subjectslist = new ArrayList<Subjects>();
        subjectslist.add(srcsubject);
        subjectslist.add(destsubject);
        Intent intent = new IntentBuilder().setKey(intentKey).setActions(actionlist).setSubjects(subjectslist).build();
        gbpTenantPolicyCreator = new GBPTenantPolicyCreator(mockBroker, intent);

        EndpointGroupId endpointGrpSrc = new EndpointGroupId(UNIQUE_ID);
        TenantId tenantId = new TenantId(UNIQUE_ID);
        L2BridgeDomainId l2bdId = new L2BridgeDomainId(UNIQUE_ID);
        Ipv4Address TEST_IPV4 = new Ipv4Address(TEST_IP);
        L3ContextId l3cntxtId = new L3ContextId(UNIQUE_ID);
        L3Address l3Address = new L3AddressBuilder().setL3Context(l3cntxtId).setIpAddress(new IpAddress(TEST_IPV4)).build();
        l3List = new ArrayList<L3Address>();
        l3List.add(l3Address);
        Endpoint endpointSrc = new EndpointBuilder().setEndpointGroup(endpointGrpSrc).setTenant(tenantId).setL2Context(l2bdId).setL3Address(l3List).build();
        final List<Endpoint> endPointList = new ArrayList<Endpoint>();
        endPointList.add(endpointSrc);
        Endpoints endPoints = new EndpointsBuilder().setEndpoint(endPointList).build();
        InstanceIdentifier<Endpoints> instanceIdentifier = InstanceIdentifier.builder(Endpoints.class).build();

        when(mockBroker.newReadOnlyTransaction()).thenReturn(mockReadTransaction);
        when(mockReadTransaction.read(LogicalDatastoreType.OPERATIONAL, instanceIdentifier)).thenReturn(mockFuture);
        when(mockFuture.checkedGet()).thenReturn(mockOptionalDataObject);
        when(mockOptionalDataObject.isPresent()).thenReturn(true);
        when(mockOptionalDataObject.get()).thenReturn(endPoints);
        when(mockBroker.newWriteOnlyTransaction()).thenReturn(mockWriteTransaction);
        when(mockWriteTransaction.submit()).thenReturn(mockCheckedFuture);
    }

    /**
     * Test method for {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}.
     *
     * Verifying the Group based Policy tenant policy creator takes an intent with action type of ALLOW and
     * converts it to a tenant policy which then gets pushed into the config datastore
     * for the groupbasedpolicy rendering to the appropriate network devices.
     *
     */
    @Test
    public void testGetAllowSubject() throws Exception {
        gbpTenantPolicyCreator.processIntentToGBP();
        gbpTenantPolicyCreator = PowerMockito.spy(gbpTenantPolicyCreator);
       /**
        * PowerMockito.verifyPrivate() is verifying the getAllowSubject method in GBPTenantPolicyCreator.
        */
       PowerMockito.verifyPrivate(gbpTenantPolicyCreator, times(3)).invoke("getAllowSubject");
    }

    /**
     * Test method for {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}.
     *
     * Verifying the Group based Policy tenant policy creator takes an intent with action type of BLOCK and
     * converts it to a tenant policy which then gets pushed into the config datastore
     * for the groupbasedpolicy rendering to the appropriate network devices.
     *
     */
    @Test
    public void testGetBlockSubject() throws Exception {
       actionlist.remove(actionAllow);
       actionlist.add(actionBlock);
       Intent intent = new IntentBuilder().setKey(intentKey).setActions(actionlist).setSubjects(subjectslist).build();
       gbpTenantPolicyCreator = new GBPTenantPolicyCreator(mockBroker, intent);
       gbpTenantPolicyCreator.processIntentToGBP();
       gbpTenantPolicyCreator = PowerMockito.spy(gbpTenantPolicyCreator);
       /**
        * PowerMockito.verifyPrivate() is verifying the getBlockSubject method in GBPTenantPolicyCreator.
        */
       PowerMockito.verifyPrivate(gbpTenantPolicyCreator, times(1)).invoke("getBlockSubject");
    }

    /**
     * Test method for {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}.
     *
     * Verifying Tenant is created or not.
     */
    @Test
    public void testGetTenant() throws Exception {
       gbpTenantPolicyCreator.processIntentToGBP();
       gbpTenantPolicyCreator = PowerMockito.spy(gbpTenantPolicyCreator);
       /**
        * PowerMockito.verifyPrivate() is verifying the getTenant method in GBPTenantPolicyCreator.
        */
       PowerMockito.verifyPrivate(gbpTenantPolicyCreator, times(3)).invoke("getTenant");
    }

    /**
     * Test method for {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}.
     *
     * Verifying getDefaultContract method is invoked or not in GbpTenantPolicyCreator.
     */
    @Test
    public void testgetDefaultContract() throws Exception {
       gbpTenantPolicyCreator.processIntentToGBP();
       gbpTenantPolicyCreator = PowerMockito.spy(gbpTenantPolicyCreator);
       /**
        * PowerMockito.verifyPrivate() is verifying the getDefaultContract method in GBPTenantPolicyCreator.
        */
       PowerMockito.verifyPrivate(gbpTenantPolicyCreator, times(2)).invoke("getDefaultContract");
    }

    /**
     * Test method for {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}.
     *
     * Verifying getEndpointIdentifier method is invoked or not in GbpTenantPolicyCreator.
     */
    @Test
    public void testGetEndpointIdentifier() throws Exception {
       gbpTenantPolicyCreator.processIntentToGBP();
       gbpTenantPolicyCreator = PowerMockito.spy(gbpTenantPolicyCreator);
       /**
        * PowerMockito.verifyPrivate() is verifying the getEndpointIdentifier method in GBPTenantPolicyCreator.
        */
       PowerMockito.verifyPrivate(gbpTenantPolicyCreator, times(2)).invoke("getEndpointIdentifier", srcsubject);
    }

    /**
     * Test method for {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}.
     *
     * Verifying createSubNet method is invoked or not in GbpTenantPolicyCreator.
     */
    @Test
    public void testCreateSubnet() throws Exception {
       final NetworkDomainId networkDomainId = new NetworkDomainId(GBPRendererHelper.createUniqueId());
       final L2FloodDomainId floodDomainId = new L2FloodDomainId(GBPRendererHelper.createUniqueId());
       gbpTenantPolicyCreator.processIntentToGBP();
       gbpTenantPolicyCreator = PowerMockito.spy(gbpTenantPolicyCreator);
       /**
        * PowerMockito.verifyPrivate() is verifying the createSubnet method in GBPTenantPolicyCreator.
        */
       PowerMockito.verifyPrivate(gbpTenantPolicyCreator, times(2)).invoke("createSubnet", l3List, networkDomainId, floodDomainId);
    }

    /**
     * Test method for {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}.
     *
     * Verifying the list of endpoints that matches an intent subject id or not
     */
    @Test
    public void testReadEPNodes() throws Exception {
       gbpTenantPolicyCreator.processIntentToGBP();
       gbpTenantPolicyCreator = PowerMockito.spy(gbpTenantPolicyCreator);
       /**
        * PowerMockito.verifyPrivate() is verifying the readEPNodes method in GBPTenantPolicyCreator.
        */
       PowerMockito.verifyPrivate(gbpTenantPolicyCreator, times(2)).invoke("readEPNodes", UNIQUE_ID);
    }

    /**
     * Test method for {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}.
     *
     * verifying the intent having id,actions and subjects or not.
     *
     */
    @Test
    public void testVerifyIntent() throws Exception {
        /**
         * Mock object for Intent.
         */
        Intent mockIntent = mock(Intent.class);
        GBPTenantPolicyCreator gbpTPCId = new GBPTenantPolicyCreator(mockBroker, mockIntent);
        gbpTPCId.processIntentToGBP();
        verify(mockIntent).getId();
        Intent actionsIntent = new IntentBuilder().setKey(intentKey).setActions(null).setSubjects(subjectslist).build();
        GBPTenantPolicyCreator gbpTPCActions = new GBPTenantPolicyCreator(mockBroker, actionsIntent);
        gbpTPCActions.processIntentToGBP();
        Intent subjectsIntent = new IntentBuilder().setKey(intentKey).setActions(actionlist).setSubjects(null).build();
        GBPTenantPolicyCreator gbpTPCSubjects = new GBPTenantPolicyCreator(mockBroker, subjectsIntent);
        gbpTPCSubjects.processIntentToGBP();
        List<Subjects> emptySubjectslist = new ArrayList<Subjects>();
        Intent emptySubjectsIntent = new IntentBuilder().setKey(intentKey).setActions(actionlist).setSubjects(emptySubjectslist).build();
        GBPTenantPolicyCreator emptyGbpTPCSubjects = new GBPTenantPolicyCreator(mockBroker, emptySubjectsIntent);
        emptyGbpTPCSubjects.processIntentToGBP();
        gbpTenantPolicyCreator = PowerMockito.spy(gbpTPCSubjects);
        /**
         * PowerMockito.verifyPrivate() is verifying the verifyIntent method in GBPTenantPolicyCreator.
         */
        PowerMockito.verifyPrivate(gbpTenantPolicyCreator, times(3)).invoke("verifyIntent");
    }

    /**
     * Test method for {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}.
     *
     * verify the matching end points available or not for a particular Subject ID.
     */
    @Test
    public void testGetTenantEndpointAttributes() throws Exception {
       EndPointGroup src = new EndPointGroupBuilder().setName("d2d86574-2d97-419e-a7e2-e1042249629c").build();
       EndPointGroup dest = new EndPointGroupBuilder().setName("d2d86574-2d97-419e-a7e2-e1042249629c").build();
       Subject srcAddress = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder().setEndPointGroup(src).build();
       Subject destAddress = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder().setEndPointGroup(dest).build();
       srcsubject = new SubjectsBuilder().setSubject(srcAddress).build();
       destsubject = new SubjectsBuilder().setSubject(destAddress).build();
       subjectslist = new ArrayList<Subjects>();
       subjectslist.add(srcsubject);
       subjectslist.add(destsubject);
       Intent intent = new IntentBuilder().setKey(intentKey).setActions(actionlist).setSubjects(subjectslist).build();
       gbpTenantPolicyCreator = new GBPTenantPolicyCreator(mockBroker, intent);
       gbpTenantPolicyCreator.processIntentToGBP();
       gbpTenantPolicyCreator = PowerMockito.spy(gbpTenantPolicyCreator);
       /**
        * PowerMockito.verifyPrivate() is verifying the getTenantEndpointAttributes method in GBPTenantPolicyCreator.
        */
       PowerMockito.verifyPrivate(gbpTenantPolicyCreator, times(2)).invoke("getTenantEndpointAttributes", UNIQUE_ID, true);
    }
}
