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
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
//        if (dataBroker != null) {
//            WriteTransaction writeTransactionNodes = dataBroker.newWriteOnlyTransaction();
//            writeTransactionNodes.delete(LogicalDatastoreType.OPERATIONAL, GBP_RENDERER_NODE_IID);
//            Futures.addCallback(writeTransactionNodes.submit(), new FutureCallback<Void>() {
//                @Override
//                public void onSuccess(Void result) {
//                    LOG.debug("Delete result: " + result);
//                }
//
//                @Override
//                public void onFailure(Throwable t) {
//                    LOG.error("Delete failed", t);
//                }
//            });
//        }
        executor.shutdown();
    }

    private void initializeGbpRendererTopology(LogicalDatastoreType type) {
        // TODO initialize gbp topology
//        InstanceIdentifier<Intents> path = InstanceIdentifier
//                .create(Intent.class)
//                .child(Intents.class, new IntentKey(new Uuid("asd")));
    }

    private void initializeIntentTopology(LogicalDatastoreType type) {
     // TODO initialize intent topology
        InstanceIdentifier<Intents> path = InstanceIdentifier.builder(Intents.class).build();
    }

}
