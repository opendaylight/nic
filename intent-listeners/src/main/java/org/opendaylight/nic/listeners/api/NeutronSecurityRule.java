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

    public String getSecurityRuleID();

    public String getSecurityGroupID();

    public String getRemoteGroupID();

    public String getSecurityTenantID();

    public Class<? extends DirectionBase> getSecurityRuleDirection();

    public Class<? extends ProtocolBase> getSecurityRuleProtocol();

    public Class<? extends EthertypeBase> getSecurityRuleEthertype();

    public IpPrefix getSecurityRuleRemoteIpPrefix();

    public Integer getSecurityRulePortMin();

    public Integer getSecurityRulePortMax();
}
