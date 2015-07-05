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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.EndpointGroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.L2BridgeDomainId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.L3ContextId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.TenantId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.endpoint.rev140421.Endpoints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.endpoint.rev140421.endpoint.fields.L3Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.Tenant;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.EndpointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * JUnit test for {@link GBPRendererHelper}.
 */
public class GBPRendererHelperTest {

    /**
     * String declaration for IPv4 Prefix.
     */
    private static final String ipv4Prefix = "192.0.2.0/24";

    /**
     * String declaration for IPv6 Prefix.
     */
    private static final String ipv6Prefix = "2001:db8:3c4d::/48";

    /**
     * String declaration for IPv6 Address.
     */
    private static final String ipv6 = "2001:db8:3c4d::";

    /**
     * String declaration for IPv4 Address.
     */
    private static final String ipv4 = "192.0.2.0";

    /**
     * String declaration for Error Messages.
     */
    private static final String[] errMsg = {"Cannot be null or empty.",
        "It Should raise excepiton before this statement"};

    /**
     * Test case for {@link GBPRendererHelper#createIntentIid()} is called to
     * check Intent ID is created.
     */
    @Test
    public void testCreateIntentIid() {
        /**
         * Here checking createIntentIid() returning InstanceIdentifier object
         * of type Intent.
         */
        InstanceIdentifier<Intent> instanceIdentifierIntent = GBPRendererHelper
                .createIntentIid();
        assertEquals(instanceIdentifierIntent.getTargetType().getName(),
                Intent.class.getName());
    }

    /**
     * Test case for {@link GBPRendererHelper#createTenantIid()} is called to
     * check Tenant ID is created.
     */
    @Test
    public void testCreateTenantIid() {
        /**
         * String declaration for Tenant ID.
         */
        final String TENANT_UUID = "b9a13232-525e-4d8c-be21-cd65e3436037";

        /**
         * Here checking createTenantIid() returning InstanceIdentifier object
         * of type Tenant with given Tenant Id.
         */
        InstanceIdentifier<Tenant> instanceIdentifierTenant = GBPRendererHelper
                .createTenantIid(new TenantId(TENANT_UUID));

        assertTrue(instanceIdentifierTenant.toString().contains(TENANT_UUID));
        assertEquals(instanceIdentifierTenant.getTargetType().getName(),
                Tenant.class.getName());
    }

    /**
     * Test case for {@link GBPRendererHelper#createEndPointGroupIid()} is
     * called to check EndpointGroup ID is created.
     */
    @Test
    public void testCreateEndPointGroupIid() {
        /**
         * String declaration for EndpointGroup ID.
         */
        final String ENDPOINTGROUP_UUID = "b9a13232-525e-4d8c-be21-cd65e3436034";

        /**
         * Here checking createEndPointGroupIid() returning InstanceIdentifier
         * object of type EndpointGroup with given EndpointGroup Id.
         */
        InstanceIdentifier<EndpointGroup> instanceIdentifierEndPointID = GBPRendererHelper
                .createEndPointGroupIid(new EndpointGroupId(ENDPOINTGROUP_UUID));

        assertTrue(instanceIdentifierEndPointID.toString().contains(
                ENDPOINTGROUP_UUID));
        assertEquals(instanceIdentifierEndPointID.getTargetType().getName(),
                EndpointGroup.class.getName());
    }

    /**
     * Test case for {@link GBPRendererHelper#createEndpointsIdentifier()} is
     * called to check End points Identifier is created.
     */
    @Test
    public void testCreateEndpointsIdentifier() {
        /**
         * Here checking createEndpointsIdentifier() returning
         * InstanceIdentifier object of type Endpoints.
         */
        InstanceIdentifier<Endpoints> instanceIdentifierEndPoint = GBPRendererHelper
                .createEndpointsIdentifier();
        assertEquals(instanceIdentifierEndPoint.getTargetType().getName(),
                Endpoints.class.getName());
    }

    /**
     * Test case for {@link GBPRendererHelper#contains()} is called to check
     * L3Address.
     */
    @Test
    public void testContainsL3Address() {
        /**
         * Mock object for L3Address.
         */
        L3Address l3Address = mock(L3Address.class);

        /**
         * List of empty L3Address.
         */
        final List<L3Address> emptyL3AddressList = new ArrayList<L3Address>();

        /**
         * List of L3Address.
         */
        final List<L3Address> l3AddressesList = new ArrayList<L3Address>();

        /**
         * Invalid Scenario - List<L3Address> is empty and L3Address as null
         * then contains() should return false only.
         */
        Boolean isFound = GBPRendererHelper.contains(emptyL3AddressList, null);
        assertFalse(isFound);

        /**
         * Valid Scenario - Valid List<L3Address> and Valid L3Address then
         * contains() should return true only. Mockito value for L3Address.
         */
        l3AddressesList.add(l3Address);
        when(l3Address.getIpAddress()).thenReturn(
                new IpAddress(new Ipv4Address(ipv4)));

        isFound = GBPRendererHelper.contains(l3AddressesList, l3Address);
        assertTrue(isFound);
    }

    /**
     * Test case for {@link GBPRendererHelper#contains()} is called to check
     * L2BridgeDomainId.
     */
    @Test
    public void testContainsL2BridgeDomainId() {
        /**
         * String declaration for L2BridgeDomain ID.
         */
        final String BRIDGEDOMAIN_UUID = "b9a13232-525e-4d8c-be21-cd65e3436035";

        /**
         * List of empty L2BridgeDomainId.
         */
        final List<L2BridgeDomainId> emptyL2BridgeDomainIdList = new ArrayList<L2BridgeDomainId>();

        /**
         * List of L2BridgeDomainId.
         */
        final List<L2BridgeDomainId> l2BridgeDomainIdList = new ArrayList<L2BridgeDomainId>();

        /**
         * Object of L2BridgeDomainId.
         */
        final L2BridgeDomainId l2DomainId = new L2BridgeDomainId(
                BRIDGEDOMAIN_UUID);

        /**
         * Invalid Scenario - List<L2BridgeDomainId> is empty and
         * L2BridgeDomainId as null then contains() should return false only.
         */
        Boolean isFound = GBPRendererHelper.contains(emptyL2BridgeDomainIdList,
                null);
        assertFalse(isFound);

        /**
         * Valid Scenario - Valid List<L2BridgeDomainId> and Valid
         * L2BridgeDomainId then contains() should return true only.
         */
        l2BridgeDomainIdList.add(l2DomainId);
        assertTrue(GBPRendererHelper.contains(l2BridgeDomainIdList, l2DomainId));
    }

    /**
     * Test case for {@link GBPRendererHelper#contains()} is called to check
     * L3ContextId
     */
    @Test
    public void testContainsL3ContextId() {
        /**
         * String declaration for ID.
         */
        final String L3CONTEXT_UUID = "b9a13232-525e-4d8c-be21-cd65e3436036";

        /**
         * List of empty L3ContextId.
         */
        final List<L3ContextId> emptyL3ContextIdsList = new ArrayList<L3ContextId>();

        /**
         * List of L3ContextId.
         */
        List<L3ContextId> l3ContextIdsList = new ArrayList<L3ContextId>();

        /**
         * Object of L3ContextId.
         */
        L3ContextId l3ContextId = new L3ContextId(L3CONTEXT_UUID);

        /**
         * Invalid Scenario - List<L3ContextId> is empty and L3ContextId as null
         * then contains() should return false only.
         */
        Boolean isFound = GBPRendererHelper.contains(emptyL3ContextIdsList,
                null);
        assertFalse(isFound);

        /**
         * Valid Scenario - Valid List<L3ContextId> and Valid L3ContextId then
         * contains() should return true only.
         */
        l3ContextIdsList.add(l3ContextId);
        assertTrue(GBPRendererHelper.contains(l3ContextIdsList, l3ContextId));
    }

    /**
     * Test case for {@link GBPRendererHelper#createUniqueId()} is called to
     * check whether Unique ID is instance of String.
     */
    @Test
    public void testCreateUniqueId() {
        /**
         * Here checking createUniqueId(), it should return unique id of type
         * String in every time calling.
         */
        assertTrue(GBPRendererHelper.createUniqueId() instanceof String);
        assertNotEquals(GBPRendererHelper.createUniqueId(),
                GBPRendererHelper.createUniqueId());
    }

    /**
     * Test case for {@link GBPRendererHelper#createIpPrefix()} is called to
     * check ipv4Prefix and ipv6Prefix.
     */
    @Test
    public void testCreateIpPrefix() throws IllegalArgumentException {

        /**
         * Valid Scenario - valid CIDR string contains Ipv4 - createIpPrefix()
         * should return IpPrefix object.
         */
        IpPrefix actualIpPrefix = GBPRendererHelper.createIpPrefix(ipv4Prefix);
        IpPrefix expectedIpPrefix = new IpPrefix(new Ipv4Prefix(ipv4Prefix));
        assertEquals(expectedIpPrefix, actualIpPrefix);

        /**
         * Valid Scenario - valid CIDR string contains Ipv6 - createIpPrefix()
         * should return IpPrefix object.
         */
        actualIpPrefix = GBPRendererHelper.createIpPrefix(ipv6Prefix);
        expectedIpPrefix = new IpPrefix(new Ipv6Prefix(ipv6Prefix));
        assertEquals(expectedIpPrefix, actualIpPrefix);

        /**
         * Invalid Scenario - empty CIDR string - createIpPrefix() should throw
         * IllegalArgumentException Exception.
         */
        try {
            GBPRendererHelper.createIpPrefix("");
            fail(errMsg[1]);
        } catch (IllegalArgumentException iax) {
            assertEquals(iax.getMessage(), errMsg[0]);
        }

        /**
         * Invalid Scenario - instead of CIDR string, passing null -
         * createIpPrefix() should throw IllegalArgumentException Exception.
         */
        try {
            GBPRendererHelper.createIpPrefix(null);
            fail(errMsg[1]);
        } catch (IllegalArgumentException iax) {
            assertEquals(iax.getMessage(), errMsg[0]);
        }
        /**
         * Invalid Scenario - invalid CIDR string - createIpPrefix() should
         * throw IllegalArgumentException Exception.
         */
        try {
            GBPRendererHelper.createIpPrefix("2001:db8:3c4d:");
            fail(errMsg[1]);
        } catch (IllegalArgumentException iax) {
            assertEquals(iax.getMessage(), "Bad format.");
        }
    }

    /**
     * Test case for {@link GBPRendererHelper#createIpAddress()} is called to
     * check ipv4 Address and ipv6 Address.
     */
    @Test
    public void testCreateIpAddress() throws IllegalArgumentException {
        /**
         * Valid Scenario - valid IP of type string contains Ipv4 -
         * createIpAddress() should return IpAddress object.
         */
        IpAddress actualIpAddress = GBPRendererHelper.createIpAddress(ipv4);
        IpAddress expectedIpAddress = new IpAddress(new Ipv4Address(ipv4));
        assertEquals(expectedIpAddress, actualIpAddress);

        /**
         * Valid Scenario - valid IP of type string contains Ipv6 -
         * createIpAddress() should return IpAddress object.
         */
        actualIpAddress = GBPRendererHelper.createIpAddress(ipv6);
        expectedIpAddress = new IpAddress(new Ipv6Address(ipv6));
        assertEquals(expectedIpAddress, actualIpAddress);

        /**
         * Invalid Scenario - if we pass empty String instead of IP -
         * createIpAddress(), should throw IllegalArgumentException Exception.
         */
        try {
            GBPRendererHelper.createIpAddress("");
            fail(errMsg[1]);
        } catch (IllegalArgumentException iax) {
            assertEquals(iax.getMessage(), errMsg[0]);
        }

        /**
         * Invalid Scenario - if we pass null instead of IP - createIpAddress(),
         * should throw IllegalArgumentException Exception.
         */
        try {
            GBPRendererHelper.createIpAddress(null);
            fail(errMsg[1]);
        } catch (IllegalArgumentException iax) {
            assertEquals(iax.getMessage(), errMsg[0]);
        }
    }

    /**
     * Test case for {@link GBPRendererHelper#getStringIpPrefix()} is called to
     * check ipv4Prefix and ipv6Prefix.
     */
    @Test
    public void testGetStringIpPrefix() {
        /**
         * Valid Scenario - if we pass valid IPPrefix contains Ipv4 -
         * getStringIpPrefix() should return value of type String.
         */
        IpPrefix ipPrefix = new IpPrefix(new Ipv4Prefix(ipv4Prefix));
        String actualIpPrefix = GBPRendererHelper.getStringIpPrefix(ipPrefix);
        String expectedIpPrefix = ipPrefix.getIpv4Prefix().getValue();
        assertEquals(expectedIpPrefix, actualIpPrefix);

        /**
         * Valid Scenario - if we pass valid IPPrefix contains Ipv6 -
         * getStringIpPrefix() should return value of type String.
         */
        ipPrefix = new IpPrefix(new Ipv6Prefix(ipv6Prefix));
        actualIpPrefix = GBPRendererHelper.getStringIpPrefix(ipPrefix);
        expectedIpPrefix = ipPrefix.getIpv6Prefix().getValue();
        assertEquals(expectedIpPrefix, actualIpPrefix);
    }

    /**
     * Test case for {@link GBPRendererHelper#getStringIpAddress()} is called to
     * check ipv4 Address and ipv6 Address.
     */
    @Test
    public void testGetStringIpAddress() {
        /**
         * Valid Scenario - if we pass valid IpAddress contains Ipv4 -
         * getStringIpAddress() should return value of type String.
         */
        IpAddress ipAddress = new IpAddress(new Ipv4Address(ipv4));
        String actualIpAddress = GBPRendererHelper.getStringIpAddress(ipAddress);
        String expectedIpAddress = ipAddress.getIpv4Address().getValue();
        assertEquals(expectedIpAddress, actualIpAddress);

        /**
         * Valid Scenario - if we pass valid IpAddress contains Ipv6 -
         * getStringIpAddress() should return value of type String.
         */
        ipAddress = new IpAddress(new Ipv6Address(ipv6));
        actualIpAddress = GBPRendererHelper.getStringIpAddress(ipAddress);
        expectedIpAddress = ipAddress.getIpv6Address().getValue();
        assertEquals(expectedIpAddress, actualIpAddress);
    }

    /**
     * Test case for {@link GBPRendererHelper#normalizeUuid()}.
     */
    @Test
    public void testNormalizeUuid() {
        /**
         * String declaration for ID.
         */
        final String inputUUID = "1aA2bB3c1aA21aA21aA21aA2bB3cC4dD";
        /**
         * Here checking that normalizeUuid() formats the given String according
         * to specified pattern.
         */
        String actualUUID = GBPRendererHelper.normalizeUuid(inputUUID);
        String expectedUUID = "1aA2bB3c-1aA2-1aA2-1aA2-1aA2bB3cC4dD";
        assertEquals(expectedUUID, actualUUID);
    }
}
