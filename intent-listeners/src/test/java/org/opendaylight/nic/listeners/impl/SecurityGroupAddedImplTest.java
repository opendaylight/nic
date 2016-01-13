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
import org.opendaylight.nic.listeners.api.SecurityGroupAdded;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import utils.SecurityGroupUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@PrepareForTest({SecurityGroupAddedImpl.class})
@RunWith(PowerMockRunner.class)
/**
 * Created by yrineu on 12/01/16.
 */
public class SecurityGroupAddedImplTest {

    private SecurityGroupAddedImpl securityGroupAddedMock;

    @Before
    public void setUp() {
        SecurityGroupUtils.setUp();
        securityGroupAddedMock = PowerMockito.spy(new SecurityGroupAddedImpl(SecurityGroupUtils.securityGroupMock));
    }

    @Test
    public void testParametersNotNull() {
        assertNotNull(securityGroupAddedMock.getSecurityGroupID());
        assertNotNull(securityGroupAddedMock.getSecurityGroupName());
        assertNotNull(securityGroupAddedMock.getSecurityGroupDescription());
        assertNotNull(securityGroupAddedMock.getSecurityTenantID());
        assertNotNull(securityGroupAddedMock.getSercurityRules());
        assertNotNull(securityGroupAddedMock.getTimeStamp());
    }

    @Test
    public void testVerifySecurityGroup() {
        assertEquals(securityGroupAddedMock.getSecurityGroupID(), SecurityGroupUtils.GROUP_ID.getValue());
        assertEquals(securityGroupAddedMock.getSecurityGroupName(), SecurityGroupUtils.GROUP_NAME);
        assertEquals(securityGroupAddedMock.getSecurityGroupDescription(), SecurityGroupUtils.GROUP_DESC);
        assertEquals(securityGroupAddedMock.getSecurityTenantID(), SecurityGroupUtils.TENANT_ID.getValue());
        assertEquals(securityGroupAddedMock.getSercurityRules().get(0), SecurityGroupUtils.FIRST_RULE);
        assertEquals(securityGroupAddedMock.getSercurityRules().get(1), SecurityGroupUtils.SECOND_RULE);

        assertTrue(securityGroupAddedMock instanceof NeutronSecurityGroupImpl);
        assertTrue(securityGroupAddedMock instanceof SecurityGroupAdded);
    }
}
