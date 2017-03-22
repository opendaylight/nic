/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.nic.engine.service.TransactionHandlerService;
import org.opendaylight.nic.exception.TransactionNotFoundException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.IntentStateTransactions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.IntentStateTransactionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransactionHandlerServiceImpl implements TransactionHandlerService {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionHandlerServiceImpl.class);

    private DataBroker dataBroker;
    private static final InstanceIdentifier<IntentStateTransactions> INTENT_STATE_TRANSACTION_IDENTIFIER =
            InstanceIdentifier.builder(IntentStateTransactions.class).build();

    public TransactionHandlerServiceImpl(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public CheckedFuture<Void, TransactionCommitFailedException> sendTransaction(final IntentStateTransaction transaction) {
        final List<IntentStateTransaction> transactions = listStateTransactions();
        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        transactions.add(transaction);
        final IntentStateTransactions buildedTransactions = new IntentStateTransactionsBuilder()
                .setIntentStateTransaction(transactions).build();
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, INTENT_STATE_TRANSACTION_IDENTIFIER, buildedTransactions);
        return writeTransaction.submit();
    }

    @Override
    public void destroyTransaction(IntentStateTransaction transaction) {
        dataBroker.newWriteOnlyTransaction()
                .delete(LogicalDatastoreType.CONFIGURATION, INTENT_STATE_TRANSACTION_IDENTIFIER);
    }

    @Override
    public CheckedFuture<Void, TransactionCommitFailedException> storeStateChange(final IntentStateTransaction transaction) {
        CheckedFuture<Void, TransactionCommitFailedException> result = null;
        final List<IntentStateTransaction> transactions = listStateTransactions();
        for (IntentStateTransaction retrieved : transactions) {
            if (retrieved.getIntentId().equals(transaction.getIntentId())) {
                result = sendTransaction(transaction);
                break;
            }
        }
        return result;
    }

    @Override
    public IntentStateTransaction retrieveTransaction(String id) throws TransactionNotFoundException {
        final Set<IntentStateTransaction> result = new HashSet<>();
        final List<IntentStateTransaction> transactions = listStateTransactions();
        IntentStateTransaction toReturn;
        transactions.forEach(transaction -> {
            if (transaction.getIntentId().equals(id)) {
                result.add(transaction);
            }
        });
        if (result.iterator().hasNext()) {
            toReturn = result.iterator().next();
        } else {
            throw new TransactionNotFoundException("Transaction with ID " + id + " not found.");
        }
        return toReturn;
    }

    private List<IntentStateTransaction> listStateTransactions() {
        List<IntentStateTransaction> result = Lists.newArrayList();

        try {
            ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
            Optional<IntentStateTransactions> transactions = readOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION,
                    INTENT_STATE_TRANSACTION_IDENTIFIER).checkedGet();
            if (transactions.isPresent()) {
                result = transactions.get().getIntentStateTransaction();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return result;
    }
}
