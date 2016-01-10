/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.NeutronSecurityGroup;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.groups.attributes.security.groups.SecurityGroup;

import java.util.List;

/**
 * See OpenStack Network API v2.0 Reference for description of
 * various Security Group attributes. The current fields are as follows:
 * <p>
 * security_group_id    uuid-str unique ID for the security group.
 * name                 String name of the security group.
 * description          String name of the security group.
 * tenant_id            uuid-str Owner of security rule..
 * security_group_rules List&lt;NeutronSecurityRuleImpl&gt; nested in the sec group.
 */

public class NeutronSecurityGroupImpl implements NeutronSecurityGroup {
    protected final SecurityGroup secGroup;

    public NeutronSecurityGroupImpl(SecurityGroup secGroup) {
        this.secGroup = secGroup;
    }

    public String getSecurityGroupID() {
        return secGroup.getUuid().getValue();
    }

    public String getSecurityGroupName() {
        return secGroup.getName();
    }

    public String getSecurityGroupDescription() {
        return secGroup.getDescription();
    }

    public String getSecurityTenantID() {
        return secGroup.getTenantId().getValue();
    }

    public List<Uuid> getSercurityRules() {
        return secGroup.getSecurityRules();
    }

}
