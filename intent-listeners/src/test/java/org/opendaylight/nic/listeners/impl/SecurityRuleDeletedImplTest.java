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
import org.opendaylight.nic.listeners.api.SecurityRuleDeleted;
import org.opendaylight.nic.listeners.api.SecurityRuleUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.rules.attributes.security.rules.SecurityRule;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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

    @Mock
    private SecurityRule securityRuleMock;

    @Before
    public void setUp() {
        securityRuleDeleted = PowerMockito.spy(new SecurityRuleDeletedImpl(securityRuleMock));
    }

    @Test
    public void testVerifyParameters() {
        assertNotNull(securityRuleDeleted.getTimeStamp());
        assertEquals(securityRuleMock, securityRuleDeleted.secRule);

        assertTrue(securityRuleDeleted instanceof NeutronSecurityRuleImpl);
        assertTrue(securityRuleDeleted instanceof SecurityRuleDeleted);
    }
}
