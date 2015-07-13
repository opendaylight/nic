/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.gbp.renderer.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import org.powermock.reflect.Whitebox;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.TenantBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.Contract;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.EndpointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.Subnet;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.ActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.AllowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.BlockBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.SubjectsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.Subject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * JUnit test for {@link GBPTenantPolicyCreator}.
 *
 * GBPTenantPolicyCreator test class is to test Group based Policy tenant policy
 * creator takes an intent and converts it to a tenant policy which then gets
 * pushed into the config datastore for the groupbasedpolicy rendering to the
 * appropriate network devices.
 */
@RunWith(PowerMockRunner.class)
public class GBPTenantPolicyCreatorTest {

    /**
     * Mock object for DataBroker.
     */
    @Mock
    private DataBroker mockBroker;
    /**
     * Mock object for Intent.
     */
    @Mock
    private Intent mockIntent;
    /**
     * Mock object for ReadOnlyTransaction.
     */
    @Mock
    private ReadOnlyTransaction mockReadTransaction;
    /**
     * Mock object for WriteTransaction.
     */
    @Mock
    private WriteTransaction mockWriteTransaction;
    /**
     * Mock object for Optional.
     */
    @Mock
    private Optional<Endpoints> mockOptionalDataObject;
    /**
     * Mock object for CheckedFuture.
     */
    @Mock
    private CheckedFuture<Optional<Endpoints>, ReadFailedException> mockFuture;
    /**
     * Mock object for CheckedFuture.
     */
    @Mock
    private CheckedFuture<Void, TransactionCommitFailedException> mockCheckedFuture;
    /**
     * String declaration for UniqueId.
     */
    private static final String UNIQUE_ID = "891fc7a8-cca7-45ee-9128-3294b96307d0";
    /**
     * String declaration for expected Subject.
     */
    private static final String Expect_Subject = "s1";
    /**
     * String declaration for expected Consumer.
     */
    private static final String Expect_Consumer = "cns1";
    /**
     * String declaration for expected Provider.
     */
    private static final String Expect_Provider = "pns1";
    /**
     * String declaration for IP Address.
     */
    private static final String TEST_IP = "192.168.196.3";
    /**
     * String declaration for actual object.
     */
    private static String actual;
    /**
     * Intent object reference.
     */
    private Intent intent;
    /**
     * IntentKey object reference.
     */
    private IntentKey intentKey;
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
        EndPointGroup src = new EndPointGroupBuilder().setName(UNIQUE_ID)
                .build();
        EndPointGroup dest = new EndPointGroupBuilder().setName(UNIQUE_ID)
                .build();
        Subject srcAddress = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder()
                .setEndPointGroup(src).build();
        Subject destAddress = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder()
                .setEndPointGroup(dest).build();
        srcsubject = new SubjectsBuilder().setSubject(srcAddress).build();
        destsubject = new SubjectsBuilder().setSubject(destAddress).build();
        subjectslist = new ArrayList<Subjects>();
        subjectslist.add(srcsubject);
        subjectslist.add(destsubject);
        intent = new IntentBuilder().setKey(intentKey)
                .setActions(actionlist).setSubjects(subjectslist).build();
        gbpTenantPolicyCreator = new GBPTenantPolicyCreator(mockBroker, intent);
        gbpTenantPolicyCreator = PowerMockito.spy(gbpTenantPolicyCreator);

        EndpointGroupId endpointGrpSrc = new EndpointGroupId(UNIQUE_ID);
        TenantId tenantId = new TenantId(UNIQUE_ID);
        L2BridgeDomainId l2bdId = new L2BridgeDomainId(UNIQUE_ID);
        Ipv4Address TEST_IPV4 = new Ipv4Address(TEST_IP);
        L3ContextId l3cntxtId = new L3ContextId(UNIQUE_ID);
        L3Address l3Address = new L3AddressBuilder().setL3Context(l3cntxtId)
                .setIpAddress(new IpAddress(TEST_IPV4)).build();
        l3List = new ArrayList<L3Address>();
        l3List.add(l3Address);
        Endpoint endpointSrc = new EndpointBuilder()
                .setEndpointGroup(endpointGrpSrc).setTenant(tenantId)
                .setL2Context(l2bdId).setL3Address(l3List).build();
        final List<Endpoint> endPointList = new ArrayList<Endpoint>();
        endPointList.add(endpointSrc);
        Endpoints endPoints = new EndpointsBuilder().setEndpoint(endPointList)
                .build();
        InstanceIdentifier<Endpoints> instanceIdentifier = InstanceIdentifier
                .builder(Endpoints.class).build();

        when(mockBroker.newReadOnlyTransaction()).thenReturn(
                mockReadTransaction);
        when(
                mockReadTransaction.read(LogicalDatastoreType.OPERATIONAL,
                        instanceIdentifier)).thenReturn(mockFuture);
        when(mockFuture.checkedGet()).thenReturn(mockOptionalDataObject);
        when(mockOptionalDataObject.isPresent()).thenReturn(true);
        when(mockOptionalDataObject.get()).thenReturn(endPoints);
        when(mockBroker.newWriteOnlyTransaction()).thenReturn(
                mockWriteTransaction);
        when(mockWriteTransaction.submit()).thenReturn(mockCheckedFuture);
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}
     * .
     *
     * Verifying the Group based Policy tenant policy creator takes an intent
     * and converts it to a tenant policy which then
     * gets pushed into the config datastore for the groupbasedpolicy rendering
     * to the appropriate network devices.
     *
     */
    @Test
    public void testProcessIntentToGBP() throws Exception {

        gbpTenantPolicyCreator.processIntentToGBP();
        verify(mockBroker, times(2)).newReadOnlyTransaction();
        verify(mockBroker).newWriteOnlyTransaction();
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}
     * .
     *
     * Verifying the Group based Policy tenant policy creator takes an intent
     * with action type of ALLOW and converts it to a tenant policy which then
     * gets pushed into the config datastore for the groupbasedpolicy rendering
     * to the appropriate network devices.
     *
     */
    @Test
    public void testGetAllowSubject() throws Exception {

        /**
         * PowerMockito.verifyPrivate() is verifying the getAllowSubject method
         * in GBPTenantPolicyCreator.
         */
        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "getAllowSubject");
        org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.Subject actual = Whitebox
                .invokeMethod(gbpTenantPolicyCreator, "getAllowSubject");
        assertEquals(Expect_Subject, actual.getName().getValue());
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}
     * .
     *
     * Verifying the Group based Policy tenant policy creator takes an intent
     * with action type of BLOCK and converts it to a tenant policy which then
     * gets pushed into the config datastore for the groupbasedpolicy rendering
     * to the appropriate network devices.
     *
     */
    @Test
    public void testGetBlockSubject() throws Exception {

        actionlist.set(0, actionBlock);
        Intent intent = new IntentBuilder().setKey(intentKey)
                .setActions(actionlist).setSubjects(subjectslist).build();
        gbpTenantPolicyCreator = new GBPTenantPolicyCreator(mockBroker, intent);
        gbpTenantPolicyCreator.processIntentToGBP();
        gbpTenantPolicyCreator = PowerMockito.spy(gbpTenantPolicyCreator);
        /**
         * PowerMockito.verifyPrivate() is verifying the getBlockSubject method
         * in GBPTenantPolicyCreator.
         */
        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "getBlockSubject");
        org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.Subject actual = Whitebox
                .invokeMethod(gbpTenantPolicyCreator, "getBlockSubject");
        assertEquals(Expect_Subject, actual.getName().getValue());
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}
     * .
     *
     * Verifying Tenant is created or not.
     */
    @Test
    public void testGetTenant() throws Exception {

        /**
         * PowerMockito.verifyPrivate() is verifying the getTenant method in
         * GBPTenantPolicyCreator.
         */
        TenantBuilder builder = Whitebox.invokeMethod(gbpTenantPolicyCreator,
                "getTenant");

        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "getTenant");
        final String actual_consumer = builder.getEndpointGroup().get(0).getConsumerNamedSelector().get(0).getName().getValue();
        final String actual_Provider = builder.getEndpointGroup().get(1).getProviderNamedSelector().get(0).getName().getValue();
        assertEquals(Expect_Consumer, actual_consumer);
        assertEquals(Expect_Provider, actual_Provider);
        assertEquals(UNIQUE_ID, builder.getId().getValue());
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}
     * .
     *
     * Verifying getDefaultContract method is invoked or not in
     * GbpTenantPolicyCreator.
     */
    @Test
    public void testgetDefaultContract() throws Exception {

        /**
         * PowerMockito.verifyPrivate() is verifying the getDefaultContract
         * method in GBPTenantPolicyCreator.
         */
        Contract mockContract = Whitebox.invokeMethod(gbpTenantPolicyCreator,
                "getDefaultContract");

        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "getDefaultContract");
        assertEquals(Expect_Subject, mockContract.getSubject().get(0).getName()
                .getValue());
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}
     * .
     *
     * Verifying getEndpointIdentifier method is invoked or not in
     * GbpTenantPolicyCreator.
     */
    @Test
    public void testGetEndpointIdentifier() throws Exception {

        /**
         * PowerMockito.verifyPrivate() is verifying the getEndpointIdentifier
         * method in GBPTenantPolicyCreator.
         */
        actual = Whitebox.invokeMethod(gbpTenantPolicyCreator,
                "getEndpointIdentifier", srcsubject);

        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "getEndpointIdentifier", srcsubject);
        assertEquals(UNIQUE_ID, actual);
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}.
     *
     * Verifying getEndpointIdentifier method is invoked or not if subject is EndPointGroup in.
     * GbpTenantPolicyCreator.
     */
    @Test
    public void testGetEndpointIdentifierForEndPointGroup() throws Exception {

        /**
         * PowerMockito.verifyPrivate() is verifying the getEndpointIdentifier
         * method in GBPTenantPolicyCreator.
         */
        actual = Whitebox.invokeMethod(gbpTenantPolicyCreator,
                "getEndpointIdentifier", srcsubject);

        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "getEndpointIdentifier", srcsubject);
        assertEquals(UNIQUE_ID, actual);
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}.
     *
     * Verifying getEndpointIdentifier method is invoked or not if subject is EndPointSelector in
     * GbpTenantPolicyCreator.
     */
    @Test
    public void testGetEndpointIdentifierForEndPointSelector() throws Exception {

        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.selector.EndPointSelector selector = mock(org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.selector.EndPointSelector.class);
        EndPointSelector EndPointSelector = mock(EndPointSelector.class);
        when(selector.getEndPointSelector()).thenReturn(UNIQUE_ID);
        when(EndPointSelector.getEndPointSelector()).thenReturn(selector);
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects subject = mock(org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects.class);
        when(subject.getSubject()).thenReturn(EndPointSelector);
        /**
         * PowerMockito.verifyPrivate() is verifying the getEndpointIdentifier
         * method in GBPTenantPolicyCreator.
         */
        final String actual = Whitebox.invokeMethod(gbpTenantPolicyCreator,
                "getEndpointIdentifier", subject);

        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "getEndpointIdentifier", subject);
        assertEquals(UNIQUE_ID, actual);
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}.
     *
     * Verifying getEndpointIdentifier method is invoked or not if subject is EndPointGroupSelector in
     * GbpTenantPolicyCreator.
     */
    @Test
    public void testGetEndpointIdentifierForEndPointGroupSelector() throws Exception {

        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.selector.EndPointGroupSelector grpSelector = mock(org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.selector.EndPointGroupSelector.class);
        EndPointGroupSelector EndPointGroupSelector = mock(EndPointGroupSelector.class);
        when(grpSelector.getEndPointGroupSelector()).thenReturn(UNIQUE_ID);
        when(EndPointGroupSelector.getEndPointGroupSelector()).thenReturn(grpSelector);
        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects subject = mock(org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects.class);
        when(subject.getSubject()).thenReturn(EndPointGroupSelector);

        /**
         * PowerMockito.verifyPrivate() is verifying the getEndpointIdentifier
         * method in GBPTenantPolicyCreator.
         */
        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "getEndpointIdentifier", subject);
        actual = Whitebox.invokeMethod(gbpTenantPolicyCreator,
                "getEndpointIdentifier", subject);
        assertEquals(UNIQUE_ID, actual);
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}
     * .
     *
     * Verifying getEndpointIdentifier method is invoked or not in
     * GbpTenantPolicyCreator.
     */
    @Test
    public void testGetEndpointIdentifierNull() throws Exception {

        org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects subject = mock(org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects.class);
        /**
         * PowerMockito.verifyPrivate() is verifying the getEndpointIdentifier
         * method in GBPTenantPolicyCreator.
         */
        actual = Whitebox.invokeMethod(gbpTenantPolicyCreator,
                "getEndpointIdentifier", subject);

        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "getEndpointIdentifier", subject);
        assertEquals("", actual);
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}
     * .
     *
     * Verifying createSubNet method is invoked or not in
     * GbpTenantPolicyCreator and checks IP Address.
     */
    @Test
    public void testCreateSubnet() throws Exception {

        final String expectedIp = "192.168.196.1";
        final String expectedIpPrefix = "192.168.196.1/24";
        final NetworkDomainId networkDomainId = new NetworkDomainId(UNIQUE_ID);
        final L2FloodDomainId floodDomainId = new L2FloodDomainId(UNIQUE_ID);
        /**
         * PowerMockito.verifyPrivate() is verifying the createSubnet method in
         * GBPTenantPolicyCreator.
         */
        Subnet mockSubnet = Whitebox.invokeMethod(gbpTenantPolicyCreator,
                "createSubnet", l3List, networkDomainId, floodDomainId);

        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "createSubnet", l3List, networkDomainId, floodDomainId);
        assertEquals(expectedIp, mockSubnet.getVirtualRouterIp()
                .getIpv4Address().getValue());
        assertEquals(expectedIpPrefix, mockSubnet.getIpPrefix().getIpv4Prefix().getValue());
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}.
     *
     * Verifying the list of endpoints that matches an intent subject id or not
     */
    @Test
    public void testReadEPNodes() throws Exception {

        /**
         * PowerMockito.verifyPrivate() is verifying the readEPNodes method in
         * GBPTenantPolicyCreator.
         */
        List<Endpoint> endPointList = Whitebox.invokeMethod(gbpTenantPolicyCreator,
                "readEPNodes", UNIQUE_ID);

        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "readEPNodes", UNIQUE_ID);
        assertEquals(UNIQUE_ID, endPointList.get(0).getTenant().getValue());
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}
     * .
     *
     * verifying the intent having id,actions and subjects or not.
     *
     */
    @Test
    public void testVerifyIntent() throws Exception {

        /**
         * PowerMockito.verifyPrivate() is verifying the verifyIntent method in
         * GBPTenantPolicyCreator.
         */
        boolean actual = Whitebox.invokeMethod(gbpTenantPolicyCreator, "verifyIntent");

        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "verifyIntent");
        assertTrue(actual);
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}.
     *
     * verifying the intent having id or not.
     *
     */
    @Test
    public void testVerifyIntentId() throws Exception {

        GBPTenantPolicyCreator gbpTPCId = new GBPTenantPolicyCreator(
                mockBroker, mockIntent);
        gbpTPCId.processIntentToGBP();
        /**
         * PowerMockito.verifyPrivate() is verifying the verifyIntent method in
         * GBPTenantPolicyCreator.
         */
        gbpTenantPolicyCreator = PowerMockito.spy(gbpTPCId);
        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "verifyIntent");
        boolean actual = Whitebox.invokeMethod(gbpTPCId, "verifyIntent");
        assertFalse(actual);
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}.
     *
     * verifying the intent having actions or not.
     *
     */
    @Test
    public void testVerifyIntentAction() throws Exception {

        when(mockIntent.getId()).thenReturn(mock(Uuid.class));
        when(mockIntent.getActions()).thenReturn(null);
        GBPTenantPolicyCreator gbpTPCAction = new GBPTenantPolicyCreator(
                mockBroker, mockIntent);
        gbpTenantPolicyCreator = PowerMockito.spy(gbpTPCAction);
        boolean actual = Whitebox.invokeMethod(gbpTenantPolicyCreator, "verifyIntent");
        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "verifyIntent");
        assertFalse(actual);
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}.
     *
     * verifying the intent having subjects or not.
     *
     */
    @Test
    public void testVerifyIntentSubject() throws Exception {

        when(mockIntent.getId()).thenReturn(mock(Uuid.class));
        when(mockIntent.getActions()).thenReturn(actionlist);
        when(mockIntent.getSubjects()).thenReturn(null);
        GBPTenantPolicyCreator gbpTPCSubject = new GBPTenantPolicyCreator(
                mockBroker, mockIntent);
        gbpTenantPolicyCreator = PowerMockito.spy(gbpTPCSubject);
        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "verifyIntent");
        boolean actual = Whitebox.invokeMethod(gbpTenantPolicyCreator, "verifyIntent");
        assertFalse(actual);
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}
     * .
     *
     * verify the matching end points available or not for a particular Subject
     * ID.
     */
    @Test
    public void testGetTenantEndpointAttributesFail() throws Exception {

        EndPointGroup src = new EndPointGroupBuilder().setName(
                "d2d86574-2d97-419e-a7e2-e1042249629c").build();
        EndPointGroup dest = new EndPointGroupBuilder().setName(
                "d2d86574-2d97-419e-a7e2-e1042249629c").build();
        Subject srcAddress = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder()
                .setEndPointGroup(src).build();
        Subject destAddress = new org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroupBuilder()
                .setEndPointGroup(dest).build();
        srcsubject = new SubjectsBuilder().setSubject(srcAddress).build();
        destsubject = new SubjectsBuilder().setSubject(destAddress).build();
        subjectslist = new ArrayList<Subjects>();
        subjectslist.add(srcsubject);
        subjectslist.add(destsubject);
        Intent intent = new IntentBuilder().setKey(intentKey)
                .setActions(actionlist).setSubjects(subjectslist).build();
        gbpTenantPolicyCreator = new GBPTenantPolicyCreator(mockBroker, intent);
        gbpTenantPolicyCreator.processIntentToGBP();
        gbpTenantPolicyCreator = PowerMockito.spy(gbpTenantPolicyCreator);
        /**
         * PowerMockito.verifyPrivate() is verifying the
         * getTenantEndpointAttributes method in GBPTenantPolicyCreator.
         */
        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "getTenantEndpointAttributes", "", true);
        /**
         *  when subject Id is null then method should return flase.
         */
        boolean actual = Whitebox.invokeMethod(gbpTenantPolicyCreator,"getTenantEndpointAttributes", "", true);
        assertFalse(actual);
    }

    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}
     * .
     *
     * verify the matching end points available or not for a particular Subject
     * ID.
     */
    @Test
    public void testGetTenantEndpointAttributes() throws Exception {

        /**
         * PowerMockito.verifyPrivate() is verifying the
         * getTenantEndpointAttributes method in GBPTenantPolicyCreator.
         */
        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "getTenantEndpointAttributes", UNIQUE_ID, true);
        /**
         * when subject Id is valid then method should return true.
         */
        boolean actual = Whitebox.invokeMethod(gbpTenantPolicyCreator,
                "getTenantEndpointAttributes", UNIQUE_ID, true);
        assertTrue(actual);
    }


    /**
     * Test method for
     * {@link org.opendaylight.nic.gbp.renderer.impl.GBPTenantPolicyCreator#processIntentToGBP()}
     * .
     *
     * Verifying createEndpointGroups method is invoked or not in
     * GbpTenantPolicyCreator.
     */
    @Test
    public void testcreateEndpointGroups() throws Exception {

        /**
         * PowerMockito.verifyPrivate() is verifying the
         * getTenantEndpointAttributes method in GBPTenantPolicyCreator.
         */
        List<EndpointGroup> endpointGroups = Whitebox.invokeMethod(gbpTenantPolicyCreator,
                "createEndpointGroups");

        PowerMockito.verifyPrivate(gbpTenantPolicyCreator).invoke(
                "createEndpointGroups");
        assertEquals(UNIQUE_ID, endpointGroups.get(0).getId().getValue());
    }
}
