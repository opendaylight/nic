/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import org.opendaylight.nic.listeners.api.SecurityRuleAdded;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.constants.rev150712.DirectionBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.constants.rev150712.EthertypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.constants.rev150712.ProtocolBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.rules.attributes.security.rules.SecurityRule;

import java.sql.Timestamp;
import java.util.Date;

public class SecurityRuleAddedImpl extends NeutronSecurityRule implements SecurityRuleAdded {
    private final Timestamp timeStamp;

    public SecurityRuleAddedImpl(SecurityRule secRule) {
        super(secRule);
        Date date= new Date();
        timeStamp = new Timestamp(date.getTime());
    }

    @Override
    public Timestamp getTimeStamp() {
        return timeStamp;
    }

}
