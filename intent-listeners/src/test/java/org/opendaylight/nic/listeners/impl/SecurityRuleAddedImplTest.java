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
import org.opendaylight.nic.listeners.api.SecurityRuleUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.rules.attributes.security.rules.SecurityRule;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@PrepareForTest({SecurityGroupAddedImpl.class})
@RunWith(PowerMockRunner.class)
/**
 * Created by yrineu on 12/01/16.
 */
public class SecurityRuleAddedImplTest {

    private SecurityRuleAddedImpl securityRuleAdded;

    @Mock
    private SecurityRule securityRuleMock;

    @Before
    public void setUp() {
        securityRuleAdded = PowerMockito.spy(new SecurityRuleAddedImpl(securityRuleMock));
    }

    @Test
    public void testVerifyParameters() {
        assertNotNull(securityRuleAdded.getTimeStamp());
        assertEquals(securityRuleMock, securityRuleAdded.secRule);

        assertTrue(securityRuleAdded instanceof NeutronSecurityRuleImpl);
        assertTrue(securityRuleAdded instanceof SecurityRuleAdded);
    }
}
