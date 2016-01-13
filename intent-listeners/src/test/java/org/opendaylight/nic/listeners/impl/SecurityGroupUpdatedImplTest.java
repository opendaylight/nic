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
import org.junit.runner.RunWith;
import org.opendaylight.nic.listeners.api.SecurityGroupUpdated;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import utils.SecurityGroupUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@PrepareForTest({SecurityGroupUpdatedImpl.class})
@RunWith(PowerMockRunner.class)
/**
 * Created by yrineu on 12/01/16.
 */
public class SecurityGroupUpdatedImplTest {

    private SecurityGroupUpdatedImpl securityGroupUpdated;

    @Before
    public void setUp() {
        SecurityGroupUtils.setUp();
        securityGroupUpdated = PowerMockito.spy(new SecurityGroupUpdatedImpl(SecurityGroupUtils.securityGroupMock));
    }

    @Test
    public void testParametersNotNull() {
        assertNotNull(securityGroupUpdated.getSecurityGroupID());
        assertNotNull(securityGroupUpdated.getSecurityGroupName());
        assertNotNull(securityGroupUpdated.getSecurityGroupDescription());
        assertNotNull(securityGroupUpdated.getSecurityTenantID());
        assertNotNull(securityGroupUpdated.getSercurityRules());
        assertNotNull(securityGroupUpdated.getTimeStamp());
    }

    @Test
    public void testVerifySecurityGroup() {
        assertEquals(securityGroupUpdated.getSecurityGroupID(), SecurityGroupUtils.GROUP_ID.getValue());
        assertEquals(securityGroupUpdated.getSecurityGroupName(), SecurityGroupUtils.GROUP_NAME);
        assertEquals(securityGroupUpdated.getSecurityGroupDescription(), SecurityGroupUtils.GROUP_DESC);
        assertEquals(securityGroupUpdated.getSecurityTenantID(), SecurityGroupUtils.TENANT_ID.getValue());
        assertEquals(securityGroupUpdated.getSercurityRules().get(0), SecurityGroupUtils.FIRST_RULE);
        assertEquals(securityGroupUpdated.getSercurityRules().get(1), SecurityGroupUtils.SECOND_RULE);

        assertTrue(securityGroupUpdated instanceof NeutronSecurityGroupImpl);
        assertTrue(securityGroupUpdated instanceof SecurityGroupUpdated);
    }
}
