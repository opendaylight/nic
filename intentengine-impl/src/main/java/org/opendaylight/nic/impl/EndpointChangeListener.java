//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.impl;

import java.util.Map;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndpointChangeListener implements DataChangeListener,
        AutoCloseable {

    private static final Logger LOG = LoggerFactory
            .getLogger(EndpointChangeListener.class);
    private final DataBroker dataBroker;
    private final ListenerRegistration<DataChangeListener> endpointListener = null;

    @Override
    public void close() {

    }

    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {

        createNodes(changes.getCreatedData());
        updateNodes(changes.getUpdatedData());
        deleteNodes(changes);

    }

    public EndpointChangeListener(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
        InstanceIdentifier<Nodes> nodePath = InstanceIdentifier
                .create(Nodes.class);
        endpointListener = dataBroker.registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL, nodePath, this,
                DataChangeScope.SUBTREE);
    }

    public void createNodes(Map<InstanceIdentifier<?>, DataObject> changes) {

    }

    public void updateNodes(Map<InstanceIdentifier<?>, DataObject> changes) {

    }

    public void deleteNodes(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {

    }

    public void init() {

    }

}