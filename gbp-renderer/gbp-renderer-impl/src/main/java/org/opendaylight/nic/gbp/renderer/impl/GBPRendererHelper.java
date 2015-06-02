/*
 * Copyright (c) 2015 Inocybe Technologies, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.gbp.renderer.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.EndpointGroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.TenantId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.Tenants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.Tenant;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.TenantKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.EndpointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.EndpointGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class GBPRendererHelper {

    public static InstanceIdentifier<Intent> createIntentIid() {
        return InstanceIdentifier.builder(Intents.class)
                .child(Intent.class)
                .build();
    }

    public static InstanceIdentifier<Tenant> createTenantIid(TenantId tenantId) {
        return InstanceIdentifier.builder(Tenants.class)
                .child(Tenant.class, new TenantKey(tenantId))
                .build();
    }

    public static InstanceIdentifier<EndpointGroup> createEndPointGroupIid(EndpointGroupId endPointGroupId) {
        return InstanceIdentifier.builder(Tenants.class)
                .child(Tenant.class)
                .child(EndpointGroup.class, new EndpointGroupKey(endPointGroupId))
                .build();
    }
}
