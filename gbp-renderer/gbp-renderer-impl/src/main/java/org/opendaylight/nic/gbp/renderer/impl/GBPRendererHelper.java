/*
 * Copyright (c) 2015 Inocybe Technologies, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.gbp.renderer.impl;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.List;

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
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.Tenants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.Tenant;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.TenantKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.EndpointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.EndpointGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;


public class GBPRendererHelper {

    public static InstanceIdentifier<Intent> createIntentIid() {
        return InstanceIdentifier.builder(Intents.class)
                .child(Intent.class)
                .build();
    }

    public static InstanceIdentifier<Tenant> createTenantIid(TenantId tenantId) {
        return InstanceIdentifier.builder(Tenants.class)
                .child(Tenant.class, new TenantKey(tenantId))
                .build();
    }

    public static InstanceIdentifier<EndpointGroup> createEndPointGroupIid(EndpointGroupId endPointGroupId) {
        return InstanceIdentifier.builder(Tenants.class)
                .child(Tenant.class)
                .child(EndpointGroup.class, new EndpointGroupKey(endPointGroupId))
                .build();
    }

    public static InstanceIdentifier<Endpoints> createEndpointsIdentifier(){
        return InstanceIdentifier.builder(Endpoints.class).build();
    }

    public static Boolean contains(List<L3Address> addresses, L3Address address){
        for(L3Address a : addresses)
            if(a.getIpAddress() != null && address.getIpAddress() != null
                && getStringIpAddress(a.getIpAddress()).equals(getStringIpAddress(address.getIpAddress())))
                return true;

        return false;
    }

    public static Boolean contains(List<L2BridgeDomainId> l2Domains, L2BridgeDomainId l2Domain){
        for(L2BridgeDomainId a : l2Domains)
            if(a.getValue() != null && l2Domain.getValue() != null
                && a.getValue().equalsIgnoreCase(l2Domain.getValue()))
                return true;

        return false;
    }

    public static Boolean contains(List<L3ContextId> l3ContextIds, L3ContextId l3Context){
        for(L3ContextId a: l3ContextIds)
            if(a.getValue() != null && l3Context.getValue() != null
                && a.getValue().equalsIgnoreCase(l3Context.getValue()))
                return true;

        return false;
    }

    public static String createUniqueId(){
        return java.util.UUID.randomUUID().toString();
    }

    /**
     * This implementation does not use nameservice lookups (e.g. no DNS).
     *
     * @param cidr - format must be valid for regex in {@link Ipv4Prefix} or {@link Ipv6Prefix}
     * @return the {@link IpPrefix} having the given cidr string representation
     * @throws IllegalArgumentException - if the argument is not a valid CIDR string
     */
    public static IpPrefix createIpPrefix(String cidr) {
        checkArgument(!Strings.isNullOrEmpty(cidr), "Cannot be null or empty.");
        String[] ipAndPrefix = cidr.split("/");
        checkArgument(ipAndPrefix.length == 2, "Bad format.");
        InetAddress ip = InetAddresses.forString(ipAndPrefix[0]);
        if (ip instanceof Inet4Address) {
            return new IpPrefix(new Ipv4Prefix(cidr));
        }
        return new IpPrefix(new Ipv6Prefix(cidr));
    }

    /**
     * This implementation does not use nameservice lookups (e.g. no DNS).
     *
     * @param ipAddress - format must be valid for regex in {@link Ipv4Address} or
     *        {@link Ipv6Address}
     * @return the {@link IpAddress} having the given ipAddress string representation
     * @throws IllegalArgumentException - if the argument is not a valid IP address string
     */
    public static IpAddress createIpAddress(String ipAddress) {
        checkArgument(!Strings.isNullOrEmpty(ipAddress), "Cannot be null or empty.");
        InetAddress ip = InetAddresses.forString(ipAddress);
        if (ip instanceof Inet4Address) {
            return new IpAddress(new Ipv4Address(ipAddress));
        }
        return new IpAddress(new Ipv6Address(ipAddress));
    }

    public static String getStringIpPrefix(IpPrefix ipPrefix) {
        if (ipPrefix.getIpv4Prefix() != null) {
            return ipPrefix.getIpv4Prefix().getValue();
        }
        return ipPrefix.getIpv6Prefix().getValue();
    }

    public static String getStringIpAddress(IpAddress ipAddress) {
        if (ipAddress.getIpv4Address() != null) {
            return ipAddress.getIpv4Address().getValue();
        }
        return ipAddress.getIpv6Address().getValue();
    }

    public static String normalizeUuid(String string) {
        return string.replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)",
                "$1-$2-$3-$4-$5");
    }

}
