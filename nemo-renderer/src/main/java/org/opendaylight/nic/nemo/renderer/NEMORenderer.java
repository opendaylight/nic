/*
 * Copyright (c) 2015 Huawei, Inc and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.nemo.renderer;

import java.util.Map;
import java.util.Map.Entry;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The VTNRenderer class parse the intents received.
 */
public class NEMORenderer implements AutoCloseable, DataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(NEMORenderer.class);
    public static final InstanceIdentifier<Intents> INTENTS_IID = InstanceIdentifier.builder(Intents.class).build();
    private DataBroker dataBroker;
    private NEMOIntentParser nemoIntentParser;
    private NEMOIntentMapper nemoIntentMapper;
    private ListenerRegistration<DataChangeListener> listenerRegistration = null;

    public NEMORenderer(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        this.nemoIntentParser = new NEMOIntentParser();
        this.nemoIntentMapper = new NEMOIntentMapper();
        listenerRegistration = dataBroker.registerDataChangeListener(
                LogicalDatastoreType.CONFIGURATION, INTENTS_IID,
                this, DataChangeScope.SUBTREE);
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        this.processCreatesChanges(changes.getCreatedData());
        this.processUpdateChanges(changes.getUpdatedData());
        this.processDeleteChanges(changes);
    }

    private void processCreatesChanges(Map<InstanceIdentifier<?>, DataObject> changes) {
        for (Entry<InstanceIdentifier<?>, DataObject> created : changes.entrySet()) {
            if (created.getValue() != null && created.getValue() instanceof Intent) {
                LOG.info("Created Intent {}.", created);
                Intent intent = (Intent) created.getValue();
                this.nemoIntentMapper.map(this.nemoIntentParser.parse(intent));

            }
        }
    }

    private void processUpdateChanges(Map<InstanceIdentifier<?>, DataObject> changes) {
        for (Entry<InstanceIdentifier<?>, DataObject> updated : changes.entrySet()) {
            if (updated.getValue() != null && updated.getValue() instanceof Intent) {
                LOG.info("Updated Intent {}.", updated);
            }
        }
    }

    private void processDeleteChanges(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        for (InstanceIdentifier<?> deleted : changes.getRemovedPaths()) {
            LOG.info("Deleted Intent {}.", deleted);
        }
    }

    @Override
    public void close() throws Exception {
        if (listenerRegistration != null) {
            listenerRegistration.close();
        }
    }

}
