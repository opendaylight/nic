/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.SecurityGroupDeleted;
import org.opendaylight.nic.neutron.NeutronSecurityGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.groups.attributes.security.groups.SecurityGroup;

import java.sql.Timestamp;
import java.util.Date;

public class SecurityGroupDeletedImpl implements SecurityGroupDeleted {
    private final Timestamp timeStamp;
    private NeutronSecurityGroup securityGroup;

    public SecurityGroupDeletedImpl(SecurityGroup secGroup) {
        Date date= new Date();
        timeStamp = new Timestamp(date.getTime());
        securityGroup = new NeutronSecurityGroup();
        securityGroup.setSecurityGroupID(secGroup.getUuid().getValue());
        securityGroup.setSecurityGroupName(secGroup.getName());
        securityGroup.setSecurityTenantID(secGroup.getTenantId().getValue());
    }
    @Override
    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    @Override
    public NeutronSecurityGroup getSecurityGroup() {
        return securityGroup;
    }

}
