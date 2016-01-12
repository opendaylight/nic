/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.SecurityGroupUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.groups.attributes.security.groups.SecurityGroup;

import java.sql.Timestamp;
import java.util.Date;

public class SecurityGroupUpdatedImpl extends NeutronSecurityGroupImpl implements SecurityGroupUpdated {
    private final Timestamp timeStamp;

    public SecurityGroupUpdatedImpl(SecurityGroup secGroup) {
        super(secGroup);
        Date date= new Date();
        timeStamp = new Timestamp(date.getTime());
    }
    @Override
    public Timestamp getTimeStamp() {
        return timeStamp;
    }
}
