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
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.rev150712.Neutron;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.groups.attributes.SecurityGroups;
import org.opendaylight.yang.gen.v1.urn.opendaylight.neutron.secgroups.rev150712.security.groups.attributes.security.groups.SecurityGroup;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation define a contract between {@link SecurityGroup} data object
 * and {@link SecurityGroupAdded}, {@link SecurityGroupDeleted}
 * and {@link SecurityGroupUpdated} notifications.
 */
public class NeutronSecGroupNotificationSupplierImpl extends
        AbstractNotificationSupplierItemRoot<SecurityGroup, SecurityGroupAdded,
                SecurityGroupDeleted, SecurityGroupUpdated>
        implements IEventService {

    private static final InstanceIdentifier<SecurityGroup> NEUTRON_SEC_GROUP_IID =
            InstanceIdentifier.create(Neutron.class).child(SecurityGroups.class)
            .child(SecurityGroup.class);

    private static final Logger LOG = LoggerFactory.getLogger(NeutronSecGroupNotificationSupplierImpl.class);
    /**
     * Constructor register supplier as DataChangeLister and create wildCarded InstanceIdentifier.
     *
     * @param db                   - {@link DataBroker}
     */
    public NeutronSecGroupNotificationSupplierImpl(final DataBroker db) {
        super(db, SecurityGroup.class, LogicalDatastoreType.CONFIGURATION);
        serviceRegistry.setEventTypeService(this, EventType.SECURITY_GROUP_ADDED,
                EventType.SECURITY_GROUP_DELETED, EventType.SECURITY_GROUP_UPDATED);
    }

    @Override
    public InstanceIdentifier<SecurityGroup> getWildCardPath() {
        return NEUTRON_SEC_GROUP_IID;
    }

    @Override
    public SecurityGroupAdded createNotification(final SecurityGroup object, final InstanceIdentifier<SecurityGroup> ii) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(ii != null);
        return new SecurityGroupAddedImpl(object);
    }

    @Override
    public SecurityGroupDeleted deleteNotification(final SecurityGroup object,
                                          final InstanceIdentifier<SecurityGroup> path) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(path != null);
        return new SecurityGroupDeletedImpl(object);
    }

    @Override
    public SecurityGroupUpdated updateNotification(final SecurityGroup object,
                                          InstanceIdentifier<SecurityGroup> path) {
        Preconditions.checkArgument(object != null);
        Preconditions.checkArgument(path != null);
        return new SecurityGroupUpdatedImpl(object);
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
        return EventType.SECURITY_GROUP_ADDED;
    }

    @Override
    public EventType getDeleteEventType() {
        return EventType.SECURITY_GROUP_DELETED;
    }

    @Override
    public EventType getUpdateEventType() {
        return EventType.SECURITY_GROUP_UPDATED;
    }

    @Override
    public Class<?> getCreateImplClass() {
        return SecurityGroupAdded.class;
    }

    @Override
    public Class<?> getDeleteImplClass() {
        return SecurityGroupDeleted.class;
    }

    @Override
    public Class<?> getUpdateImplClass() {
        return SecurityGroupUpdated.class;
    }
}