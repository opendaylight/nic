/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.api;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.constants.rev150712.DirectionBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.constants.rev150712.EthertypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.constants.rev150712.ProtocolBase;

/**
 * See OpenStack Network API v2.0 Reference for description of
 * various Security Rule attributes. The current fields are as follows:
 * <p>
 * security_rule_id  uuid (String) UUID for the security group rule.
 * direction         String Direction the VM traffic  (ingress/egress).
 * security_group_id The security group to associate rule with.
 * protocol          String IP Protocol (icmp, tcp, udp, etc).
 * port_range_min    Integer Port at start of range
 * port_range_max    Integer Port at end of range
 * ethertype         String ethertype in L2 packet (IPv4, IPv6, etc)
 * remote_ip_prefix  String (IP cidr) CIDR for address range.
 * remote_group_id   uuid-str Source security group to apply to rule.
 * tenant_id         uuid-str Owner of security rule. Admin only outside tenant.
 */

public interface NeutronSecurityRule {

    /**
     * Get the security_rule_id associated with this rule
     * @return String
     */
    String getSecurityRuleID();

    /**
     * Get the security_group_id associated with this rule
     * @return String
     */
    String getSecurityGroupID();

    /**
     * Get the remote_group_id associated with this rule
     * @return String
     */
    String getRemoteGroupID();

    /**
     * Get the tenant_id associated with this rule
     * @return
     */
    String getSecurityTenantID();

    /**
     * Get the direction this rule is to be applied
     * @return Could be DirectionIngress or DirectionEgress
     */
    Class<? extends DirectionBase> getSecurityRuleDirection();

    /**
     * Get the protocol associated with this rule
     * @return Could be TCP, UDP, ICMP
     */
    Class<? extends ProtocolBase> getSecurityRuleProtocol();

    /**
     * Get the ethertype associated with this rule
     * @return String ethertype in L2 packet (IPv4, IPv6)
     */
    Class<? extends EthertypeBase> getSecurityRuleEthertype();

    /**
     * Get the remote_ip_prefix associated with this rule
     * @return String (IP cidr) CIDR for address range
     */
    IpPrefix getSecurityRuleRemoteIpPrefix();

    /**
     * Get the port_range_min associated with this rule
     * @return Integer Port at start of range
     */
    Integer getSecurityRulePortMin();

    /**
     * Get the port_range_max associated with this rule
     * @return Integer Port at end of range
     */
    Integer getSecurityRulePortMax();
}
