/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.utils;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.nic.of.renderer.flow.FlowAction;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

public final class GenericTransactionUtils {
    static final Logger logger = LoggerFactory.getLogger(GenericTransactionUtils.class);

    public static <T extends DataObject> boolean writeData(
              DataBroker dataBroker, LogicalDatastoreType logicalDatastoreType,
              InstanceIdentifier<T> iid, T dataObject, FlowAction isAdd) {
        Preconditions.checkNotNull(dataBroker);
        WriteTransaction modification = dataBroker.newWriteOnlyTransaction();
        boolean isFlowAdd = (FlowAction.ADD_FLOW.getValue() == isAdd.getValue());
        if (isFlowAdd) {
            if (dataObject == null) {
                logger.warn("Invalid attempt to add a non-existent object to path {}", iid);
                return false;
            }
            modification.merge(logicalDatastoreType, iid, dataObject, true /*createMissingParents*/);
            //TODO: Change to support more actions
        } else {
            modification.delete(LogicalDatastoreType.CONFIGURATION, iid);
        }
        CheckedFuture<Void, TransactionCommitFailedException> commitFuture = modification.submit();
        try {
            commitFuture.checkedGet();
            logger.debug("Transaction success for {} of object {}", (isFlowAdd) ? "add" : "delete", dataObject);
            return true;
        } catch (Exception e) {
            logger.error("Transaction failed with error {} for {} of object {}",
                    e.getMessage(), (isFlowAdd) ? "add" : "delete", dataObject);
            modification.cancel();
            return false;
        }
    }

    public static <T extends DataObject> T readData(DataBroker dataBroker,
                                                    LogicalDatastoreType dataStoreType, InstanceIdentifier<T> iid) {
        Preconditions.checkNotNull(dataBroker);
        ReadOnlyTransaction readTransaction = dataBroker.newReadOnlyTransaction();
        try {
            Optional<T> optionalData = readTransaction.read(dataStoreType, iid).get();
            if (optionalData.isPresent()) {
                return (T)optionalData.get();
            }
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Read transaction for identifier {} failed with error {}", iid, e.getMessage());
            readTransaction.close();
        }
        return null;
    }
}
