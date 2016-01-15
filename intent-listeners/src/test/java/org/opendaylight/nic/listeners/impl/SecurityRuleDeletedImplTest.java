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
import org.opendaylight.nic.listeners.api.SecurityRuleAdded;
import org.opendaylight.nic.listeners.api.SecurityRuleDeleted;
import org.opendaylight.nic.listeners.api.SecurityRuleUpdated;
import org.opendaylight.nic.neutron.NeutronSecurityRule;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.rules.attributes.security.rules.SecurityRule;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import utils.SecurityRuleUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@PrepareForTest({SecurityRuleDeletedImpl.class})
@RunWith(PowerMockRunner.class)
/**
 * Created by yrineu on 12/01/16.
 */
public class SecurityRuleDeletedImplTest {

    private SecurityRuleDeletedImpl securityRuleDeleted;
    private NeutronSecurityRule neutronSecurityRule;

    @Before
    public void setUp() {
        SecurityRuleUtils.setUp();
        securityRuleDeleted = PowerMockito.spy(new SecurityRuleDeletedImpl(SecurityRuleUtils.securityRuleMock));
        neutronSecurityRule = securityRuleDeleted.getSecurityRule();
    }

    @Test
    public void testParametersNotNull() {
        assertNotNull(neutronSecurityRule.getSecurityGroupID());
        assertNotNull(neutronSecurityRule.getSecurityRuleID());
        assertNotNull(neutronSecurityRule.getSecurityTenantID());
        assertNotNull(neutronSecurityRule.getSecurityRulePortMax());
        assertNotNull(neutronSecurityRule.getSecurityRulePortMin());
        assertNotNull(securityRuleDeleted.getTimeStamp());
    }

    @Test
    public void testVerifyParameters() {
        assertEquals(neutronSecurityRule.getSecurityRuleID(), SecurityRuleUtils.RULE_ID.getValue());
        assertEquals(neutronSecurityRule.getSecurityGroupID(), SecurityRuleUtils.GROUP_ID.getValue());
        assertEquals(neutronSecurityRule.getSecurityTenantID(), SecurityRuleUtils.TENANT_ID.getValue());
        assertEquals(neutronSecurityRule.getSecurityRulePortMin(), SecurityRuleUtils.PORT_MIN);
        assertEquals(neutronSecurityRule.getSecurityRulePortMax(), SecurityRuleUtils.PORT_MAX);
        assertTrue(securityRuleDeleted instanceof SecurityRuleDeleted);
    }
}
