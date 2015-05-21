package org.opendaylight.nic.gbp.renderer.impl;

import java.util.Map.Entry;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker.DataChangeScope;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GBPRendererDataChangeListener implements DataChangeListener,
        AutoCloseable {

    private static final Logger LOG = LoggerFactory
            .getLogger(GBPRendererDataChangeListener.class);
    private DataBroker dataBroker;
    private ListenerRegistration<DataChangeListener> gbpRendererListener = null;

    public GBPRendererDataChangeListener(DataBroker dataBroker) {
        this.dataBroker = dataBroker;

        gbpRendererListener = dataBroker.registerDataChangeListener(
                LogicalDatastoreType.OPERATIONAL,
                GBPRendererConstants.INTENT_IID, this, DataChangeScope.SUBTREE);
    }

    @Override
    public void onDataChanged(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        create(changes);
        update(changes);
        delete(changes);
    }

    private void delete(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        for (Entry<InstanceIdentifier<?>, DataObject> updated : changes
                .getUpdatedData().entrySet()) {
            LOG.info("Delete requested on intent id {}", updated.getKey());
            // TODO implement delete, verify old data versus new data
        }
    }

    private void update(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        for (Entry<InstanceIdentifier<?>, DataObject> updated : changes
                .getUpdatedData().entrySet()) {
            LOG.info("Intent {} has been modified.", updated.getKey());
            // TODO implement update
        }
    }

    private void create(
            AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> changes) {
        for (Entry<InstanceIdentifier<?>, DataObject> created : changes
                .getCreatedData().entrySet()) {
            LOG.info("New intent added with id {}.", created.getKey());
            // TODO implement create
        }
    }

    @Override
    public void close() throws Exception {
        // TODO Auto-generated method stub
        LOG.info("GBPDataChangeListener closed.");
        if (gbpRendererListener != null) {
            gbpRendererListener.close();
        }
    }

}
