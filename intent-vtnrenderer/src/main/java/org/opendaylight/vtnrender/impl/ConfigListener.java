/*
 * Copyright (c) 2015 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.vtnrender.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.vtn.manager.VTNException;
import org.opendaylight.vtn.manager.util.EtherAddress;

import org.opendaylight.vtn.manager.internal.TxContext;
import org.opendaylight.vtn.manager.internal.TxQueue;

import org.opendaylight.vtn.manager.internal.util.ChangedData;
import org.opendaylight.vtn.manager.internal.util.DataStoreListener;
import org.opendaylight.vtn.manager.internal.util.IdentifiedData;
import org.opendaylight.vtn.manager.internal.util.XmlConfigFile;
import org.opendaylight.vtn.manager.internal.util.tx.AbstractTxTask;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;

/**
 * VTN Render configuration listener for configuration view.
 */
public final class ConfigListener extends DataStoreListener<Intents, Void> {
    /**
     * Logger instance.
     */
    private static final Logger  LOG = LoggerFactory.getLogger(ConfigListener.class);

    /**
     * Construct a new instance.
     *    
     * @param broker  A {@link DataBroker} service instance.    
     */

    public ConfigListener(DataBroker broker) {
        super(Intents.class);
        registerListener(broker, LogicalDatastoreType.CONFIGURATION,
                         DataChangeScope.SUBTREE);
    }

     /**
     * {@inheritDoc}
     */
    @Override
    protected Void enterEvent (
        AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> ev) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void exitEvent(Void ectx) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreated(Void ectx, IdentifiedData<Intents> data) {
        Intents intents = data.getValue();
        IntentParser intentParser = new IntentParser();
        intentParser.IntentParserVtnRenderer(intents);
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onUpdated(Void ectx, ChangedData<Intents> data) {
        onCreated(ectx, data);
        System.out.println("updated Method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onRemoved(Void ectx, IdentifiedData<Intents> data) {
        System.out.println("Removed Method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected InstanceIdentifier<Intents> getWildcardPath() {
        return null;        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
