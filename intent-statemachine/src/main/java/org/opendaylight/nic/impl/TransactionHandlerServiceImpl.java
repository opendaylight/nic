/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.nic.engine.service.TransactionHandlerService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.IntentStateTransactions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.IntentStateTransactionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransactionBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;

public class TransactionHandlerServiceImpl implements TransactionHandlerService {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionHandlerServiceImpl.class);

    private DataBroker dataBroker;
    private static final InstanceIdentifier<IntentStateTransactions> INTENT_STATE_TRANSACTION_IDENTIFIER =
            InstanceIdentifier.builder(IntentStateTransactions.class).build();

    public TransactionHandlerServiceImpl(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public void sendTransaction(final IntentStateTransaction transaction) {
        final List<IntentStateTransaction> transactions = listStateTransactions();
        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        transactions.add(transaction);
        final IntentStateTransactions buildedTransactions = new IntentStateTransactionsBuilder()
                .setIntentStateTransaction(transactions).build();
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, INTENT_STATE_TRANSACTION_IDENTIFIER, buildedTransactions);

        Futures.addCallback(writeTransaction.submit(), new FutureCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void aVoid) {
                LOG.info("\nTransaction pushed with success!");
            }

            @Override
            public void onFailure(Throwable throwable) {
                //TODO: Create a rollback
                LOG.info("\nError when try to push transaction!");
            }
        });
    }

    @Override
    public void destroyTransaction(IntentStateTransaction transaction) {
        dataBroker.newWriteOnlyTransaction()
                .delete(LogicalDatastoreType.OPERATIONAL, INTENT_STATE_TRANSACTION_IDENTIFIER);
    }

    @Override
    public void storeStateChange(final String id, final String newState, final String event) {
        final List<IntentStateTransaction> transactions = listStateTransactions();
        for (IntentStateTransaction retrieved : transactions) {
            if (retrieved.getIntentId().equals(id)) {
                sendTransaction(buildNewTransactionBy(retrieved, newState, event));
                break;
            }
        }
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

    private IntentStateTransaction buildNewTransactionBy(final IntentStateTransaction stateTransaction,
                                                         final String newState,
                                                         final String event) {
        return new IntentStateTransactionBuilder()
                .setIntentId(stateTransaction.getIntentId())
                .setReceivedEvent(event)
                .setCurrentState(newState).build();
    }
}
