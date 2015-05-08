/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent.Status;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.IntentBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class NicProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(NicProvider.class);

    private ProviderContext providerContext;
    public static final InstanceIdentifier<Intent> INTENTS_NODEID = InstanceIdentifier.builder(Intents.class).child(Intent.class).build();
    private DataBroker dataBroker;
    private NicProviderDataChangeListener dataChangeListener;

    @Override
    public void onSessionInitiated(ProviderContext session) {
        this.providerContext = session;
        this.dataBroker = providerContext.getSALService(DataBroker.class);

        dataChangeListener = new NicProviderDataChangeListener(this.dataBroker);

        //Initialize the data store
        IntentBuilder intentBuilder = new IntentBuilder().setStatus(Status.CompletedSuccess);

        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.CONFIGURATION, INTENTS_NODEID, intentBuilder.build());
        tx.submit();

        LOG.info("NicProvider Session Initiated");
    }

    @Override
    public void close() throws Exception {

        if (dataBroker != null) {
            WriteTransaction writeTransactionNodes = dataBroker.newWriteOnlyTransaction();
            writeTransactionNodes.delete(LogicalDatastoreType.CONFIGURATION, INTENTS_NODEID);

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

        if(dataChangeListener != null)
            dataChangeListener.close();

        LOG.info("Intent provider closed");
    }

    public void AddIntent(Intent intent){

        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.CONFIGURATION, INTENTS_NODEID, intent);

        //Submit this intent asynchronously
        Futures.addCallback(tx.submit(), new FutureCallback<Void>() {

            @Override
            public void onFailure(Throwable t) {
                LOG.info("Intent operation: new intent add failed");

            }

            @Override
            public void onSuccess(Void result) {
                LOG.info("Intent operation: new intent added successfully");

            }

        });
    }

}
