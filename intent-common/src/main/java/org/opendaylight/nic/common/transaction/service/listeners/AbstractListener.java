/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.listeners;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;

import java.util.Collection;
import java.util.List;

/**
 * Created by yrineu on 30/06/17.
 */
public abstract class AbstractListener<T extends DataObject, E extends DataObject>
        implements NicDataTreeChangesListener<T> {

    private DataBroker dataBroker;
    private ListenerRegistration registration;

    AbstractListener(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void handleTreeEvent(Collection<DataTreeModification<T>> collection) {
        collection.forEach(dataTree -> {
            final DataObjectModification<T> objectModification = dataTree.getRootNode();
            switch (dataTree.getRootNode().getModificationType()) {
                case WRITE:
                    handleTreeCreated(objectModification.getDataAfter());
                    break;
                case SUBTREE_MODIFIED:
                    handleSubTreeChange(objectModification.getDataBefore(), objectModification.getDataAfter());
                    break;
                case DELETE:
                    handleTreeRemoved(objectModification.getDataBefore());
                    break;
            }
        });
    }

    /**
     * Handle Intent created
     *
     * @param data a {@link T}
     */
    abstract void handleTreeCreated(T data);

    /**
     * Handle Intent updated
     *
     * @param before a {@link T}
     * @param after  a {@link T}
     */
    abstract void handleSubTreeChange(T before, T after);

    /**
     * Handle Intent removed
     *
     * @param intent a {@link T}
     */
    abstract void handleTreeRemoved(T intent);

    boolean isSubTreeElementAdded(final List<E> before, final List<E> after) {
        return (before.size() < after.size());
    }

    boolean isSubTreeElementRemoved(final List<E> before, final List<E> after) {
        return (before.size() > after.size());
    }

    void registerForDataTreeChanges(final DataTreeIdentifier<T> identifier) {
        registration = dataBroker.registerDataTreeChangeListener(identifier, this);
    }

    void closeDataTreeRegistration() {
        registration.close();
    }
}
