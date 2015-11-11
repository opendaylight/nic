/*
 * Copyright (c) 2015 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.mapping.mdsal.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.intent.mapping.mdsal.rev151111.Mappings;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.intent.mapping.mdsal.rev151111.MappingsBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

public class MappingMdsalProvider implements BindingAwareProvider, DataChangeListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(MappingMdsalProvider.class);
    private DataBroker dataBroker;
    private InstanceIdentifier<Mappings> iid;

    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> change) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSessionInitiated(ProviderContext session) {
        // Retrieve the data broker to create transactions
        dataBroker =  session.getSALService(DataBroker.class);
        iid = InstanceIdentifier.builder(Mappings.class).build();

        Mappings mappings = new MappingsBuilder().build();

        // Initialize operational and default config data in MD-SAL data store
        initDatastore(LogicalDatastoreType.CONFIGURATION, iid, mappings);
        initDatastore(LogicalDatastoreType.OPERATIONAL, iid, mappings);
    }

    private void initDatastore(LogicalDatastoreType store, InstanceIdentifier<Mappings> iid, Mappings mappings) {
        // Put the Mapping data to MD-SAL data store
        WriteTransaction transaction = dataBroker.newWriteOnlyTransaction();
        transaction.put(store, iid, mappings);

        // Perform the tx.submit asynchronously
        Futures.addCallback(transaction.submit(), new FutureCallback<Void>() {
            @Override
            public void onSuccess(final Void result) {
                LOG.info("initDatastore for mappings: transaction succeeded");
            }
            @Override
            public void onFailure(final Throwable throwable) {
                LOG.error("initDatastore for mappings: transaction failed");
            }
        });
        LOG.info("initDatastore: mappings data populated: {}", store, iid, mappings);
    }
}
