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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.constants.rev150712.DirectionBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.constants.rev150712.EthertypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.constants.rev150712.ProtocolBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.rules.attributes.security.rules.SecurityRule;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@PrepareForTest({NeutronSecurityRuleImpl.class})
@RunWith(PowerMockRunner.class)
/**
 * Created by yrineu on 12/01/16.
 */
public class NeutronSecurityRuleImplTest {

    private NeutronSecurityRuleImpl neutronSecurityRuleMock;

    @Mock
    private SecurityRule securityRuleMock;

    @Mock
    private IpPrefix ipPrefixMock;

    private static final Uuid RULE_ID = Uuid.getDefaultInstance(UUID.randomUUID().toString());

    private static final Uuid GROUP_ID = Uuid.getDefaultInstance(UUID.randomUUID().toString());

    private static final Uuid REMOTE_GROUP_ID = Uuid.getDefaultInstance(UUID.randomUUID().toString());

    private static final Uuid TENANT_ID = Uuid.getDefaultInstance(UUID.randomUUID().toString());

    private static final String GROUP_NAME = "GroupName";

    private static final String GROUP_DESC = "GroupDesc";

    private static final Integer RULE_PORT_MIN = Integer.MIN_VALUE;

    private static final Integer RULE_PORT_MAX = Integer.MAX_VALUE;

    @Before
    public void setUp() {
        neutronSecurityRuleMock = PowerMockito.spy(new NeutronSecurityRuleImpl(securityRuleMock));

        when(securityRuleMock.getId()).thenReturn(RULE_ID);
        when(securityRuleMock.getSecurityGroupId()).thenReturn(GROUP_ID);
        when(securityRuleMock.getRemoteGroupId()).thenReturn(REMOTE_GROUP_ID);
        when(securityRuleMock.getTenantId()).thenReturn(TENANT_ID);
        when(securityRuleMock.getRemoteIpPrefix()).thenReturn(ipPrefixMock);
        when(securityRuleMock.getPortRangeMin()).thenReturn(RULE_PORT_MIN);
        when(securityRuleMock.getPortRangeMax()).thenReturn(RULE_PORT_MAX);
    }

    @Test
    public void testParametersNotNull() {
        assertNotNull(neutronSecurityRuleMock.getSecurityTenantID());
        assertNotNull(neutronSecurityRuleMock.getSecurityGroupID());
        assertNotNull(neutronSecurityRuleMock.getRemoteGroupID());
        assertNotNull(neutronSecurityRuleMock.getSecurityRuleID());
        assertNotNull(neutronSecurityRuleMock.getSecurityRulePortMax());
        assertNotNull(neutronSecurityRuleMock.getSecurityRulePortMin());
        assertNotNull(neutronSecurityRuleMock.getSecurityRuleRemoteIpPrefix());
    }

    @Test
    public void testNeutronSecurityRuleParameters() {
        assertEquals(neutronSecurityRuleMock.getSecurityTenantID(), TENANT_ID.getValue());
        assertEquals(neutronSecurityRuleMock.getSecurityGroupID(), GROUP_ID.getValue());
        assertEquals(neutronSecurityRuleMock.getRemoteGroupID(), REMOTE_GROUP_ID.getValue());
        assertEquals(neutronSecurityRuleMock.getSecurityRuleID(), RULE_ID.getValue());
        assertEquals(neutronSecurityRuleMock.getSecurityRulePortMax(), RULE_PORT_MAX);
        assertEquals(neutronSecurityRuleMock.getSecurityRulePortMin(), RULE_PORT_MIN);
        assertEquals(neutronSecurityRuleMock.getSecurityRuleRemoteIpPrefix(), ipPrefixMock);
    }
}
