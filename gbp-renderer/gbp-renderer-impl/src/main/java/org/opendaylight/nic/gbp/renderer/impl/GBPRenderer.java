/*
 * Copyright 2015, Inocybe Technologies
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.gbp.renderer.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.IntentsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class GBPRenderer implements AutoCloseable, BindingAwareProvider {

    private static final Logger LOG = LoggerFactory
            .getLogger(GBPRenderer.class);
    private DataBroker dataBroker;
    private final ExecutorService executor;

    public GBPRenderer(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        executor = Executors.newFixedThreadPool(1);
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        LOG.info("GBP Renderer Provider Session Initiated");
        dataBroker = session.getSALService(DataBroker.class);
        initializeGbpRendererTopology(LogicalDatastoreType.OPERATIONAL);
        initializeGbpRendererTopology(LogicalDatastoreType.CONFIGURATION);
    }

    @Override
    public void close() throws Exception {
        LOG.info("GBPRenderer Closed");
        if (dataBroker != null) {
            deleteNode(GBPRendererConstants.INTENTS_IID);
            deleteNode(GBPRendererConstants.INTENT_IID);
        }
        executor.shutdown();
    }

    private void deleteNode(InstanceIdentifier<?> iid) {
        WriteTransaction writeTransactionNodes = dataBroker.newWriteOnlyTransaction();
        writeTransactionNodes.delete(LogicalDatastoreType.OPERATIONAL, iid);
        Futures.addCallback(writeTransactionNodes.submit(), new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                LOG.debug("Delete result: " + result);
            }

            @Override
            public void onFailure(Throwable t) {
                LOG.error("Delete failed", t);
            }
        });
    }

    private void initializeGbpRendererTopology(LogicalDatastoreType type) {
        ReadWriteTransaction transaction = dataBroker.newReadWriteTransaction();
        initializeIntentTopology(transaction, type);
        CheckedFuture<Optional<Intent>, ReadFailedException> intentTp = transaction
                .read(type, GBPRendererConstants.INTENT_IID);
        try {
            if (!intentTp.get().isPresent()) {
                IntentBuilder ib = new IntentBuilder();
                ib.setIntentId(GBPRendererConstants.GBP_RENDERER_INTENT_ID);
                transaction.put(type, GBPRendererConstants.INTENT_IID, ib.build());
                transaction.submit();
            } else {
                transaction.cancel();
            }
        } catch (Exception e) {
            LOG.error("Error initializing intent topology {}", e);
        }
    }

    private void initializeIntentTopology(ReadWriteTransaction transaction, LogicalDatastoreType type) {
        CheckedFuture<Optional<Intents>, ReadFailedException> intentsTp = transaction
                .read(type,GBPRendererConstants.INTENTS_IID);
        try {
            if (!intentsTp.get().isPresent()) {
                IntentsBuilder isb = new IntentsBuilder();
                transaction.put(type, GBPRendererConstants.INTENTS_IID, isb.build());
            }
        } catch (Exception e) {
            LOG.error("Error initializing intent topology {}", e);
        }
    }

}
