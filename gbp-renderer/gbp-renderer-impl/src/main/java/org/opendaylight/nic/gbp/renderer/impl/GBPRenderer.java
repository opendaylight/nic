/*
 * Copyright (c) 2015 Inocybe Technologies, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.gbp.renderer.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class GBPRenderer implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(GBPRenderer.class);

    private DataBroker dataBroker;

    protected ServiceRegistration nicConsoleRegistration;

    private GBPRendererDataChangeListener gBPRendererDataChangeListener;

    public GBPRenderer(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void init() {
        LOG.info("GBP Renderer Provider Session Initiated");

        // Initialize operational and default config data in MD-SAL data store
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        nicConsoleRegistration = context.registerService(GBPRenderer.class, this, null);

        gBPRendererDataChangeListener = new GBPRendererDataChangeListener(dataBroker);
    }

    @Override
    public void close() throws Exception {
        LOG.info("GBPRenderer Closed");
        if (dataBroker != null) {
            deleteNode(GBPRendererHelper.createIntentIid());
        }
        if (nicConsoleRegistration!= null) {
            nicConsoleRegistration.unregister();
        }
        if (gBPRendererDataChangeListener != null) {
            gBPRendererDataChangeListener.close();
        }
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
