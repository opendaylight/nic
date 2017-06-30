/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.listeners;

import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yangtools.yang.binding.DataObject;

import java.util.Collection;

/**
 * Created by yrineu on 30/06/17.
 */
public abstract class AbstractListener <T extends DataObject> {

    public void handleIntentTreeEvent(Collection<DataTreeModification<T>> collection) {
        collection.iterator().forEachRemaining(intentTree -> {
            final DataObjectModification<T> objectModification = intentTree.getRootNode();
            final T intents = objectModification.getDataAfter();
            switch (intentTree.getRootNode().getModificationType()) {
                case WRITE:
                    handleIntentCreated(intents);
                    break;
                case SUBTREE_MODIFIED:
                    handleIntentUpdated(intents);
                    break;
                case DELETE:
                    handleIntentRemoved(intents);
                    break;
            }
        });
    }

    /**
     * Handle Intent created
     * @param intent a {@link T}
     */
    abstract void handleIntentCreated(T intent);

    /**
     * Handle Intent updated
     * @param intent a {@link T}
     */
    abstract void handleIntentUpdated(T intent);

    /**
     * Handle Intent removed
     * @param intent a {@link T}
     */
    abstract void handleIntentRemoved(T intent);

}
