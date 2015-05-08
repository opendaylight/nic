/*
 * Copyright 2015, Inocybe Technologies
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.util.IntentRunningRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intent;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NicProviderDataChangeListener implements DataChangeListener,
    AutoCloseable {

    private static final Logger LOG = LoggerFactory
            .getLogger(NicProviderDataChangeListener.class);



    private ListenerRegistration<DataChangeListener> intentListener = null;
    private IntentRunningRegistry intentRegistry;
    private DataBroker dataProvider;

    public NicProviderDataChangeListener(DataBroker dataBroker){

        dataProvider = dataBroker;
        intentRegistry = IntentRunningRegistry.getInstance();
        intentListener = dataProvider.registerDataChangeListener(LogicalDatastoreType.CONFIGURATION,
                    NicProvider.INTENTS_NODEID,
                    this,
                    DataChangeScope.SUBTREE);

        LOG.debug("Initialized intent data change listener");
    }


    @Override
    public void close() throws Exception {

        if (intentListener != null) {
            intentListener.close();
        }

    }

    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {

        //Pull the list of new items
        Map<InstanceIdentifier<?>, DataObject> createdObjects = changes.getCreatedData();

        for (Entry<InstanceIdentifier<?>, DataObject> created : createdObjects.entrySet()) {
            if (created.getValue() != null
                    && created.getValue() instanceof Intent) {

                Intent intent = (Intent) created;

                synchronized (intentRegistry){
                    LOG.debug("Intent key " + intent.getId() + " was created!");
                    try {
                        intentRegistry.addIntent(intent);
                    } catch (InterruptedException e) {
                        LOG.debug("Intent update failed - " + e.getStackTrace());
                    }
                }
            }
        }

        //Pull the list of items that are being updated
        Map<InstanceIdentifier<?>, DataObject> updatedObjects = changes.getUpdatedData();

        for (Entry<InstanceIdentifier<?>, DataObject> updated : updatedObjects.entrySet()) {
            if (updated.getValue() != null
                    && updated.getValue() instanceof Intent) {

                Intent intent = (Intent) updated;

                if(createdObjects.get(updated.getKey()) == null){

                    synchronized (intentRegistry){
                        LOG.debug("Intent key " + intent.getId() + " is being updated!");
                        try {
                            intentRegistry.updateIntent(intent);
                        } catch (InterruptedException e) {
                            LOG.debug("Intent update failed - " + e.getStackTrace());
                        }
                    }
                }
            }
        }

        //Pull the intents to be deleted.
        Map<InstanceIdentifier<?>, DataObject> orginalObjects = changes.getOriginalData();
        Set<InstanceIdentifier<?>> deletedObjects = changes.getRemovedPaths();

        for (InstanceIdentifier<?> deletedIdentifier : deletedObjects) {
            if (deletedIdentifier.getTargetType().equals(Intent.class)) {

                synchronized (intentRegistry){
                    Intent intent = (Intent) orginalObjects.get(deletedIdentifier);
                    intentRegistry.removeIntent(intent);
                }
            }
        }

    }

}
