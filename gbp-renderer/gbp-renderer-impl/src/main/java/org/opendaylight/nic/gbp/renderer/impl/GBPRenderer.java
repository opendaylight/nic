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
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    }

    @Override
    public void close() throws Exception {
        LOG.info("GBPRenderer Closed");
        if (dataBroker != null) {
            deleteNode(GBPRendererConstants.INTENTS_IID);
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
}
