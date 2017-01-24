/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.nic.listeners.api.SecurityGroupDeleted;
import org.opendaylight.nic.neutron.NeutronSecurityGroup;
import utils.SecurityGroupUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by yrineu on 12/01/16.
 */
public class SecurityGroupDeleteImplTest {

    private SecurityGroupDeletedImpl securityGroupDeleted;
    private NeutronSecurityGroup neutronSecurityGroup;

    @Before
    public void setUp() {
        SecurityGroupUtils.setUp();
        securityGroupDeleted = Mockito.spy(new SecurityGroupDeletedImpl(SecurityGroupUtils.securityGroupMock));
        neutronSecurityGroup = securityGroupDeleted.getSecurityGroup();
    }

    @Test
    public void testParametersNotNull() {
        assertNotNull(neutronSecurityGroup.getSecurityGroupID());
        assertNotNull(neutronSecurityGroup.getSecurityGroupName());
        assertNotNull(neutronSecurityGroup.getSecurityTenantID());
        assertNotNull(securityGroupDeleted.getTimeStamp());
    }

    @Test
    public void testVerifySecurityGroup() {
        assertEquals(neutronSecurityGroup.getSecurityGroupID(), SecurityGroupUtils.GROUP_ID.getValue());
        assertEquals(neutronSecurityGroup.getSecurityGroupName(), SecurityGroupUtils.GROUP_NAME);
        assertEquals(neutronSecurityGroup.getSecurityTenantID(), SecurityGroupUtils.TENANT_ID.getValue());
        assertTrue(securityGroupDeleted instanceof SecurityGroupDeleted);
    }
}
