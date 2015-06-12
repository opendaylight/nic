/*
 * Copyright (c) 2015 Inocybe Technologies, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.gbp.renderer.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GBPRendererDataChangeListener implements DataChangeListener,
        AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(GBPRendererDataChangeListener.class);

    private DataBroker dataBroker;
    private ListenerRegistration<DataChangeListener> gbpRendererListener = null;

    public GBPRendererDataChangeListener(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        gbpRendererListener = dataBroker.registerDataChangeListener(
                LogicalDatastoreType.CONFIGURATION,
                GBPRendererHelper.createIntentIid(), this, DataChangeScope.SUBTREE);
    }

    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        create(changes.getCreatedData());
        update(changes.getUpdatedData());
        delete(changes);
    }

    private void create(Map<InstanceIdentifier<?>, DataObject> changes) {

        for (Entry<InstanceIdentifier<?>, DataObject> created : changes.entrySet()) {

            if (created.getValue() != null && created.getValue() instanceof Intent) {

                Intent intent = (Intent) created.getValue();

                LOG.info("New intent created with id {}.", intent);

                //This may generate conflicts since actions tend to oppose each other
                GBPTenantPolicyCreator createGBPolicy = new GBPTenantPolicyCreator(this.dataBroker, intent);

                createGBPolicy.processIntentToGBP();
            }
        }
    }

    private void update(Map<InstanceIdentifier<?>, DataObject> changes) {
        // TODO implement update
    }

    private void delete(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        // TODO implement delete, verify old data versus new data
    }

    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub
        LOG.info("GBPDataChangeListener closed.");
        if (gbpRendererListener != null) {
            gbpRendererListener.close();
        }
    }
}
