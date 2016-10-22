/*
 * Copyright (c) 2016 Open Networking Foundation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.nic.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.IntentDefinitions;
import org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.IntentDefinitionsBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class IntentEngineProviderImpl implements AutoCloseable {
    private static final Logger LOG = LoggerFactory
            .getLogger(IntentEngineProviderImpl.class);
    private ServiceRegistration<IntentEngineProviderImpl> engineRegistration;
    public static final InstanceIdentifier<IntentDefinitions> INTENTS_NBI_IID = InstanceIdentifier.builder(IntentDefinitions.class).build();
    private final DataBroker dataBroker;

    public IntentEngineProviderImpl(final DataBroker databroker) {
        this.dataBroker = databroker;
    }

    public void init() {
        try {
            final BundleContext context =
                    FrameworkUtil.getBundle(this.getClass()).getBundleContext();
            engineRegistration = context.registerService(IntentEngineProviderImpl.class, this, null);
            initIntentsConfiguration();
            initIntentsOperational();
        } catch (Exception e) {
            LOG.error("Exception in IntentEngineProviderService");
            throw e;
        }
        LOG.info("Initialization done");
    }

    /**
     * Populates Intents' initial operational data into the MD-SAL operational
     * data store.
     */
    protected void initIntentsOperational() {
        // Build the initial intents operational data
        org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.IntentDefinitions intents = new IntentDefinitionsBuilder().build();

        // Put the Intents operational data into the MD-SAL data store
        WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.OPERATIONAL, INTENTS_NBI_IID, intents);

        // Perform the tx.submit asynchronously
        Futures.addCallback(tx.submit(), new FutureCallback<Void>() {

            @Override
            public void onSuccess(final Void result) {
                LOG.info("Init Intents-NBI Operational: transaction succeeded");
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.info("Init Intents-NBI Operational: transaction failed");
            }
        });

        LOG.info("Init Intents-NBI Operational: status populated: {}", intents);
    }

    /**
     * Populates Intents' default config data into the MD-SAL configuration data
     * store. Note the database write to the tree are done in a synchronous
     * fashion
     */
    protected void initIntentsConfiguration() {
        // Build the default Intents config data
        final org.opendaylight.yang.gen.v1.urn.onf.intent.intent.nbi.rev160920.IntentDefinitions intents = new IntentDefinitionsBuilder().build();

        // Place default config data in data store tree
        final WriteTransaction tx = dataBroker.newWriteOnlyTransaction();
        tx.put(LogicalDatastoreType.CONFIGURATION, INTENTS_NBI_IID, intents);
        // Perform the tx.submit synchronously
        tx.submit();

        LOG.info("Init Intents-NBI Configuration: default config populated: {}", intents);
    }

    public List<IntentDefinitions> listIntents(final boolean isConfigurationDatastore) {
        List<IntentDefinitions> listOfIntents = null;

        try {
            final ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
            final Optional<IntentDefinitions> intents = tx.read((isConfigurationDatastore) ? LogicalDatastoreType.CONFIGURATION
                    : LogicalDatastoreType.OPERATIONAL, INTENTS_NBI_IID).checkedGet();

            if (intents.isPresent()) {
                listOfIntents = new ArrayList<>();
                listOfIntents.add(0, intents.get());
            } else {
                LOG.info("Intent-NBI tree was empty!");
            }
        } catch (Exception e) {
            LOG.error("List Intents-NBI: failed: {}", e.getMessage(), e);
        }

        if (listOfIntents == null) {
            listOfIntents = new ArrayList<>();
        }
        LOG.info("List Intents-NBI Configuration: list of intents retrieved successfully");
        return listOfIntents;
    }

    @Override
    public void close() throws Exception {
        engineRegistration.unregister();
    }
}
