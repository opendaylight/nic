/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.impl;

import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.listeners.api.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.rev150712.Neutron;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.groups.attributes.security.groups.SecurityGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.rules.attributes.SecurityRules;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.rules.attributes.security.rules.SecurityRule;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation define a contract between {@link SecurityRule} data object
 * and {@link SecurityRuleAdded}, {@link SecurityRuleDeleted}
 * and {@link SecurityRuleUpdated} notifications.
 */
public class NeutronSecRuleNotificationSupplierImpl extends
        AbstractNotificationSupplierItemRoot<SecurityRule, SecurityRuleAdded,
                SecurityRuleDeleted, SecurityRuleUpdated>
        implements IEventService {

    private static final InstanceIdentifier<SecurityRule> NEUTRON_SEC_RULE_IID =
            InstanceIdentifier.create(Neutron.class).child(SecurityRules.class)
                    .child(SecurityRule.class);

    private static final Logger LOG = LoggerFactory.getLogger(NeutronSecRuleNotificationSupplierImpl.class);
    /**
     * Constructor register supplier as DataChangeLister and create wildCarded InstanceIdentifier.
     *
     * @param db                   - {@link DataBroker}
     */
    public NeutronSecRuleNotificationSupplierImpl(final DataBroker db) {
        super(db, SecurityRule.class, LogicalDatastoreType.CONFIGURATION);
        serviceRegistry.setEventTypeService(this, EventType.SECURITY_RULE_ADDED,
                EventType.SECURITY_RULE_DELETED, EventType.SECURITY_RULE_UPDATED);
    }

    @Override
    public InstanceIdentifier<SecurityRule> getWildCardPath() {
        return NEUTRON_SEC_RULE_IID;
    }

    @Override
    public SecurityRuleAdded createNotification(final SecurityRule object, final InstanceIdentifier<SecurityRule> ii) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(ii != null);
        return null;
    }

    @Override
    public SecurityRuleDeleted deleteNotification(final SecurityRule object,
                                          final InstanceIdentifier<SecurityRule> path) {
        Preconditions.checkArgument(path != null);
        return null;
    }

    @Override
    public SecurityRuleUpdated updateNotification(final SecurityRule object,
                                          InstanceIdentifier<SecurityRule> path) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(path != null);
        return null;
    }

    @Override
    public void addEventListener(IEventListener<?> listener) {
        serviceRegistry.registerEventListener(this, listener);
    }

    @Override
    public void removeEventListener(IEventListener<?> listener) {
        serviceRegistry.unregisterEventListener(this, listener);
    }

    @Override
    public EventType getCreateEventType() {
        return EventType.SECURITY_RULE_ADDED;
    }

    @Override
    public EventType getDeleteEventType() {
        return EventType.SECURITY_RULE_DELETED;
    }

    @Override
    public EventType getUpdateEventType() {
        return EventType.SECURITY_RULE_UPDATED;
    }

    @Override
    public Class<?> getCreateImplClass() {
        return SecurityRuleAdded.class;
    }

    @Override
    public Class<?> getDeleteImplClass() {
        return SecurityRuleDeleted.class;
    }

    @Override
    public Class<?> getUpdateImplClass() {
        return SecurityRuleUpdated.class;
    }
}