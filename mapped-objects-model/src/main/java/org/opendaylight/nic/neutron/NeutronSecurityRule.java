/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.neutron;


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

public class NeutronSecurityRule {
    private String securityRuleID = null;
    private String securityGroupID = null;
    private String securityTenantID = null;
    private String securityRuleDirection = null;
    private String securityRuleProtocol = null;
    private String securityRuleEthertype = null;
    private String securityRuleRemoteIpPrefix = null;
    private Integer securityRulePortMin = null;
    private Integer securityRulePortMax = null;

    /**
     * Get the security_rule_id associated with this rule
     * @return String
     */
    public String getSecurityRuleID() {
        return securityRuleID;
    }

    /**
     * Set the security_rule_id associated with this rule
     * @param securityRuleID
     */
    public void setSecurityRuleID(String securityRuleID) {
        this.securityRuleID = securityRuleID;
    }

    /**
     * Get the security_group_id associated with this rule
     * @return String
     */
    public String getSecurityGroupID() {
        return securityGroupID;
    }

    /**
     * Set the security_group_id associated with this rule
     * @param securityGroupID
     */
    public void setSecurityGroupID(String securityGroupID) {
        this.securityGroupID = securityGroupID;
    }

    /**
     * Get the tenant_id associated with this rule
     * @return String
     */
    public String getSecurityTenantID() {
        return securityTenantID;
    }

    /**
     * Set the tenant_id associated with this rule
     * @param securityTenantID
     */
    public void setSecurityTenantID(String securityTenantID) {
        this.securityTenantID = securityTenantID;
    }

    /**
     * Get the direction this rule is to be applied
     * @return Could be DirectionIngress or DirectionEgress
     */
    public String getSecurityRuleDirection() {
        return securityRuleDirection;
    }

    /**
     * Set the direction this rule is to be applied
     * @param securityRuleDirection
     */
    public void setSecurityRuleDirection(String securityRuleDirection) {
        this.securityRuleDirection = securityRuleDirection;
    }

    /**
     * Get the protocol associated with this rule
     * @return Could be TCP, UDP, ICMP
     */
    public String getSecurityRuleProtocol() {
        return securityRuleProtocol;
    }

    /**
     * Set the protocol associated with this rule
     * @param securityRuleProtocol
     */
    public void setSecurityRuleProtocol(String securityRuleProtocol) {
        this.securityRuleProtocol = securityRuleProtocol;
    }

    /**
     * Get the ethertype associated with this rule
     * @return String ethertype in L2 packet (IPv4, IPv6)
     */
    public String getSecurityRuleEthertype() {
        return securityRuleEthertype;
    }

    /**
     * Set the ethertype associated with this rule
     * @param securityRuleEthertype
     */
    public void setSecurityRuleEthertype(String securityRuleEthertype) {
        this.securityRuleEthertype = securityRuleEthertype;
    }

    /**
     * Get the remote_ip_prefix associated with this rule
     * @return String (IP cidr) CIDR for address range
     */
    public String getSecurityRuleRemoteIpPrefix() {
        return securityRuleRemoteIpPrefix;
    }

    public void setSecurityRuleRemoteIpPrefix(String securityRuleRemoteIpPrefix) {
        this.securityRuleRemoteIpPrefix = securityRuleRemoteIpPrefix;
    }

    /**
     * Get the port_range_min associated with this rule
     * @return Integer Port at start of range
     */
    public Integer getSecurityRulePortMin() {
        return securityRulePortMin;
    }

    /**
     * Set the port_range_min associated with this rule
     * @param securityRulePortMin
     */
    public void setSecurityRulePortMin(Integer securityRulePortMin) {
        this.securityRulePortMin = securityRulePortMin;
    }

    /**
     * Get the port_range_max associated with this rule
     * @return Integer Port at end of range
     */
    public Integer getSecurityRulePortMax() {
        return securityRulePortMax;
    }

    /**
     * Set the port_range_max associated with this rule
     * @param securityRulePortMax
     */
    public void setSecurityRulePortMax(Integer securityRulePortMax) {
        this.securityRulePortMax = securityRulePortMax;
    }

}