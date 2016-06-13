/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.SecurityRuleUpdated;
import org.opendaylight.nic.neutron.NeutronSecurityRule;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.rules.attributes.security.rules.SecurityRule;

import java.sql.Timestamp;
import java.util.Date;

public class SecurityRuleUpdatedImpl implements SecurityRuleUpdated {
    private final Timestamp timeStamp;
    private NeutronSecurityRule securityRule;

    public SecurityRuleUpdatedImpl(SecurityRule secRule) {
        Date date= new Date();
        timeStamp = new Timestamp(date.getTime());
        securityRule = new NeutronSecurityRule();
        securityRule.setSecurityRuleID(secRule.getUuid().getValue());
        securityRule.setSecurityGroupID(secRule.getSecurityGroupId().getValue());
        securityRule.setSecurityTenantID(secRule.getTenantId().getValue());
        if (secRule.getDirection() != null) {
            securityRule.setSecurityRuleDirection(secRule.getDirection().getSimpleName());
        }
        if (secRule.getProtocol() != null) {
            securityRule.setSecurityRuleProtocol(String.valueOf(secRule.getProtocol().getValue()));
        }
        if (secRule.getEthertype() != null) {
            securityRule.setSecurityRuleEthertype(secRule.getEthertype().getSimpleName());
        }
        if (secRule.getRemoteIpPrefix() != null) {
            securityRule.setSecurityRuleRemoteIpPrefix(String.valueOf(secRule.getRemoteIpPrefix().getValue()));
        }
        if (secRule.getPortRangeMin() != null) {
            securityRule.setSecurityRulePortMin(secRule.getPortRangeMin());
        }
        if (secRule.getPortRangeMax() != null) {
            securityRule.setSecurityRulePortMax(secRule.getPortRangeMax());
        }
    }
    @Override
    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    @Override
    public NeutronSecurityRule getSecurityRule() {
        return securityRule;
    }

}
