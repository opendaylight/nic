/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package utils;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.rules.attributes.security.rules.SecurityRule;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SecurityRuleUtils {
    public static final Uuid GROUP_ID = Uuid.getDefaultInstance(UUID.randomUUID().toString());

    public static final Uuid RULE_ID = Uuid.getDefaultInstance(UUID.randomUUID().toString());

    public static final Integer PORT_MIN = 222;

    public static final Integer PORT_MAX = 225;

    public static final  Uuid TENANT_ID = Uuid.getDefaultInstance(UUID.randomUUID().toString());

    public static SecurityRule securityRuleMock;

    public static void setUp() {
        securityRuleMock = mock(SecurityRule.class);

        when(securityRuleMock.getId()).thenReturn(RULE_ID);
        when(securityRuleMock.getSecurityGroupId()).thenReturn(GROUP_ID);
        when(securityRuleMock.getTenantId()).thenReturn(TENANT_ID);
        when(securityRuleMock.getPortRangeMin()).thenReturn(PORT_MIN);
        when(securityRuleMock.getPortRangeMax()).thenReturn(PORT_MAX);
    }
}
