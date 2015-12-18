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
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.common.rev151010.UserId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.BeginTransactionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.EndTransactionInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.NemoIntentService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.nemo.intent.rev151010.StructureStyleNemoUpdateInputBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

/**
 *
 * @author gwu
 *
 */
public class NEMORenderer implements AutoCloseable, DataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(NEMORenderer.class);

    public static final InstanceIdentifier<Intent> INTENT_IID = InstanceIdentifier.builder(Intents.class)
            .child(Intent.class).build();

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;
    private ListenerRegistration<DataChangeListener> listenerRegistration;

    public NEMORenderer(DataBroker dataBroker0, RpcProviderRegistry rpcProviderRegistry0) {
        this.dataBroker = dataBroker0;
        this.rpcProviderRegistry = rpcProviderRegistry0;
    }

    public void init() {
        LOG.info("Initializing NEMORenderer");

        listenerRegistration = dataBroker.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION, INTENT_IID,
                this, DataChangeScope.SUBTREE);
    }

    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        create(changes.getCreatedData());
        update(changes.getUpdatedData());
        delete(changes);
    }

    private void create(Map<InstanceIdentifier<?>, DataObject> changes) {
        for (Entry<InstanceIdentifier<?>, DataObject> created : changes.entrySet()) {
            if (created.getValue() instanceof Intent) {
                LOG.info("Created Intent {}.", created);

                createOrUpdateIntent((Intent) created.getValue());
            }
        }
    }

    private void update(Map<InstanceIdentifier<?>, DataObject> changes) {
        for (Entry<InstanceIdentifier<?>, DataObject> updated : changes.entrySet()) {
            if (updated.getValue() instanceof Intent) {
                LOG.info("Updated Intent {}.", updated);

                createOrUpdateIntent((Intent) updated.getValue());
            }
        }
    }

    private void delete(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        for (InstanceIdentifier<?> deleted : changes.getRemovedPaths()) {
            LOG.info("Deleted Intent {}.", deleted);

            // TODO: handle delete
        }
    }

    public static UserId USER_ID = new UserId("f81d4fae-7dec-11d0-a765-00a0c91e6bf6");

    @VisibleForTesting
    boolean createOrUpdateIntent(Intent intent) {

        StructureStyleNemoUpdateInputBuilder builder;
        try {
            BandwidthOnDemandParameters params = NEMOIntentParser.parseBandwidthOnDemand(intent);
            builder = NemoInputBuilders.getUpdateBuilder(params);
        } catch (Exception e) {
            builder = null;
        }

        if (builder != null) {
            // make call to NEMO via MD-SAL RPC
            NemoIntentService nemoEngine = rpcProviderRegistry.getRpcService(NemoIntentService.class);

            nemoEngine.beginTransaction(new BeginTransactionInputBuilder().setUserId(USER_ID).build());

            nemoEngine.structureStyleNemoUpdate(builder.setUserId(USER_ID).build());

            nemoEngine.endTransaction(new EndTransactionInputBuilder().setUserId(USER_ID).build());

            return true;
        } else {
            LOG.info("Not a valid BoD intent");
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        if (listenerRegistration != null) {
            listenerRegistration.close();
        }
    }

}
