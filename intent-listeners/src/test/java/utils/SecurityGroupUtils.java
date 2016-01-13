/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package utils;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.groups.attributes.security.groups.SecurityGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by yrineu on 12/01/16.
 */
public class SecurityGroupUtils {

    public static final Uuid GROUP_ID = Uuid.getDefaultInstance(UUID.randomUUID().toString());

    public static final String GROUP_NAME = "GroupName";

    public static final String GROUP_DESC = "GroupDesc";

    public static final  Uuid TENANT_ID = Uuid.getDefaultInstance(UUID.randomUUID().toString());

    public static final List<Uuid> RULES = new ArrayList<Uuid>();

    public static final Uuid FIRST_RULE = Uuid.getDefaultInstance(UUID.randomUUID().toString());

    public static final Uuid SECOND_RULE = Uuid.getDefaultInstance(UUID.randomUUID().toString());

    public static SecurityGroup securityGroupMock;

    public static void setUp() {
        securityGroupMock = mock(SecurityGroup.class);

        RULES.add(FIRST_RULE);
        RULES.add(SECOND_RULE);

        when(securityGroupMock.getUuid()).thenReturn(GROUP_ID);
        when(securityGroupMock.getName()).thenReturn(GROUP_NAME);
        when(securityGroupMock.getDescription()).thenReturn(GROUP_DESC);
        when(securityGroupMock.getTenantId()).thenReturn(TENANT_ID);
        when(securityGroupMock.getSecurityRules()).thenReturn(RULES);
    }
}
