package org.opendaylight.nic.gbp.renderer.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;

public class MdsalUtils {
    private static final Logger LOG = LoggerFactory.getLogger(MdsalUtils.class);
    private DataBroker databroker = null;

    /**
     * Class constructor setting the data broker.
     *
     * @param dataBroker the {@link org.opendaylight.controller.md.sal.binding.api.DataBroker}
     */
    public MdsalUtils(DataBroker dataBroker) {
        this.databroker = dataBroker;
    }

    /**
     * Executes delete as a blocking transaction.
     *
     * @param store {@link LogicalDatastoreType} which should be modified
     * @param path {@link InstanceIdentifier} to read from
     * @param <D> the data object type
     * @return the result of the request
     */
    public <D extends org.opendaylight.yangtools.yang.binding.DataObject> boolean delete(
            final LogicalDatastoreType store, final InstanceIdentifier<D> path)  {
        boolean result = false;
        final WriteTransaction transaction = databroker.newWriteOnlyTransaction();
        transaction.delete(store, path);
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        try {
            future.checkedGet();
            result = true;
        } catch (TransactionCommitFailedException e) {
            LOG.warn("Failed to delete {} ", path, e);
        }
        return result;
    }

    /**
     * Executes merge as a blocking transaction.
     *
     * @param logicalDatastoreType {@link LogicalDatastoreType} which should be modified
     * @param path {@link InstanceIdentifier} for path to read
     * @param <D> the data object type
     * @return the result of the request
     */
    public <D extends org.opendaylight.yangtools.yang.binding.DataObject> boolean merge(
            final LogicalDatastoreType logicalDatastoreType, final InstanceIdentifier<D> path, D data)  {
        boolean result = false;
        final WriteTransaction transaction = databroker.newWriteOnlyTransaction();
        transaction.merge(logicalDatastoreType, path, data, true);
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        try {
            future.checkedGet();
            result = true;
        } catch (TransactionCommitFailedException e) {
            LOG.warn("Failed to merge {} ", path, e);
        }
        return result;
    }

    /**
     * Executes put as a blocking transaction.
     *
     * @param logicalDatastoreType {@link LogicalDatastoreType} which should be modified
     * @param path {@link InstanceIdentifier} for path to read
     * @param <D> the data object type
     * @return the result of the request
     */
    public <D extends org.opendaylight.yangtools.yang.binding.DataObject> boolean put(
            final LogicalDatastoreType logicalDatastoreType, final InstanceIdentifier<D> path, D data)  {
        boolean result = false;
        final WriteTransaction transaction = databroker.newWriteOnlyTransaction();
        transaction.put(logicalDatastoreType, path, data, true);
        CheckedFuture<Void, TransactionCommitFailedException> future = transaction.submit();
        try {
            future.checkedGet();
            result = true;
        } catch (TransactionCommitFailedException e) {
            LOG.warn("Failed to put {} ", path, e);
        }
        return result;
    }

    /**
     * Executes read as a blocking transaction.
     *
     * @param store {@link LogicalDatastoreType} to read
     * @param path {@link InstanceIdentifier} for path to read
     * @param <D> the data object type
     * @return the result as the data object requested
     */
    public <D extends org.opendaylight.yangtools.yang.binding.DataObject> D read(
            final LogicalDatastoreType store, final InstanceIdentifier<D> path)  {
        D result = null;
        final ReadOnlyTransaction transaction = databroker.newReadOnlyTransaction();
        Optional<D> optionalDataObject;
        CheckedFuture<Optional<D>, ReadFailedException> future = transaction.read(store, path);
        try {
            optionalDataObject = future.checkedGet();
            if (optionalDataObject.isPresent()) {
                result = optionalDataObject.get();
            } else {
                LOG.debug("{}: Failed to read {}",
                        Thread.currentThread().getStackTrace()[1], path);
            }
        } catch (ReadFailedException e) {
            LOG.warn("Failed to read {} ", path, e);
        }
        transaction.close();
        return result;
    }
}
