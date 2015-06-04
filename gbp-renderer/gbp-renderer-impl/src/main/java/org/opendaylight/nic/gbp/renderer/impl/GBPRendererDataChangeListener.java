/*
 * Copyright (c) 2015 Inocybe Technologies, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.gbp.renderer.impl;

import java.util.List;
import java.util.Map.Entry;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.gbp.renderer.rev150511.GbpRendererAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.gbp.renderer.rev150511.GbpRendererAugmentationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.EndpointGroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.TargetName;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.common.rev140421.UniqueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.Tenants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.Tenant;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.Contract;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.ContractBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.EndpointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.EndpointGroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.Target;
import org.opendaylight.yang.gen.v1.urn.opendaylight.groupbasedpolicy.policy.rev140421.tenants.tenant.contract.TargetBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Actions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Conditions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Subjects;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.selector.EndPointSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.selector.EndPointGroupSelector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.end.point.group.EndPointGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

public class GBPRendererDataChangeListener implements DataChangeListener,
        AutoCloseable {

    private static final Logger LOG = LoggerFactory
            .getLogger(GBPRendererDataChangeListener.class);
    @SuppressWarnings("unused")
    private DataBroker dataBroker;
    private ListenerRegistration<DataChangeListener> gbpRendererListener = null;
    private final ReadWriteTransaction transaction;

    public GBPRendererDataChangeListener(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.transaction = dataBroker.newReadWriteTransaction();
        gbpRendererListener = dataBroker.registerDataChangeListener(
                LogicalDatastoreType.CONFIGURATION,
                GBPRendererConstants.INTENTS_IID, this, DataChangeScope.SUBTREE);
    }

    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        create(changes);
        update(changes);
        delete(changes);
    }

    private void delete(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        for (Entry<InstanceIdentifier<?>, DataObject> updated : changes
                .getUpdatedData().entrySet()) {
            LOG.info("Delete requested on intent id {}", updated.getKey());
            // TODO implement delete, verify old data versus new data
        }
    }

    private void update(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        for (Entry<InstanceIdentifier<?>, DataObject> updated : changes
                .getUpdatedData().entrySet()) {
            LOG.info("Intent {} has been modified.", updated.getKey());
            // TODO implement update
        }
    }

    private void create(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        for (Entry<InstanceIdentifier<?>, DataObject> created : changes
                .getCreatedData().entrySet()) {
            LOG.info("New intent added with id {}.", created.getKey());
            if (created.getValue() != null) {
                if (created instanceof Intent) {
                    Intent intent = (Intent)created.getValue();

                    //Pull the components of the intent and activate the process to process the intent
                    List<Subjects> subjects = intent.getSubjects();
                    List<Actions> actions = intent.getActions();
                    List<Conditions> conditions = intent.getConditions();
                    List<Constraints> constraints = intent.getConstraints();

                    //This may generate conflicts since actions tend to oppose each other
                    for (Actions action: actions) {
                        GBPTenantPolicyCreator createGBPolicy = new GBPTenantPolicyCreator(this.dataBroker,
                                subjects,
                                action, conditions,
                                constraints);

                        createGBPolicy.processIntentToGBP();
                    }
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub
        LOG.info("GBPDataChangeListener closed.");
        if (gbpRendererListener != null) {;
            gbpRendererListener.close();
        }
    }
}
