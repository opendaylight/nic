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
import org.mockito.Mock;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.groups.attributes.security.groups.SecurityGroup;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@PrepareForTest({SecurityGroupAddedImpl.class})
@RunWith(PowerMockRunner.class)
/**
 * Created by yrineu on 12/01/16.
 */
public class SecurityGroupAddedImplTest {

    private SecurityGroupAddedImpl securityGroupAddedMock;

    @Mock
    private SecurityGroup securityGroupMock;

    private static final Uuid GROUP_ID = Uuid.getDefaultInstance(UUID.randomUUID().toString());

    private static final String GROUP_NAME = "GroupName";

    private static final String GROUP_DESC = "GroupDesc";

    private static final  Uuid TENANT_ID = Uuid.getDefaultInstance(UUID.randomUUID().toString());

    private static final List<Uuid> RULES = new ArrayList<Uuid>();

    private static final Uuid FIRST_RULE = Uuid.getDefaultInstance(UUID.randomUUID().toString());

    private static final Uuid SECOND_RULE = Uuid.getDefaultInstance(UUID.randomUUID().toString());

    @Before
    public void setUp() {
        RULES.add(FIRST_RULE);
        RULES.add(SECOND_RULE);
        securityGroupAddedMock = PowerMockito.spy(new SecurityGroupAddedImpl(securityGroupMock));

        when(securityGroupMock.getUuid()).thenReturn(GROUP_ID);
        when(securityGroupMock.getName()).thenReturn(GROUP_NAME);
        when(securityGroupMock.getDescription()).thenReturn(GROUP_DESC);
        when(securityGroupMock.getTenantId()).thenReturn(TENANT_ID);
        when(securityGroupMock.getSecurityRules()).thenReturn(RULES);
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
        assertEquals(securityGroupAddedMock.getSecurityGroupID(), GROUP_ID.getValue());
        assertEquals(securityGroupAddedMock.getSecurityGroupName(), GROUP_NAME);
        assertEquals(securityGroupAddedMock.getSecurityGroupDescription(), GROUP_DESC);
        assertEquals(securityGroupAddedMock.getSecurityTenantID(), TENANT_ID.getValue());
        assertEquals(securityGroupAddedMock.getSercurityRules().get(0), FIRST_RULE);
        assertEquals(securityGroupAddedMock.getSercurityRules().get(1), SECOND_RULE);
    }
}
