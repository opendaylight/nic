/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.transaction.api;

import org.opendaylight.nic.transaction.TransactionResult;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

/**
 * Created by yrineu on 07/07/16.
 */
public interface IntentCommonProviderService extends AutoCloseable {

    void start();

    void registerForExecutor(IntentTransactionListener transactionListener);

    void registerForResults(IntentTransactionResultListener resultListener);

    void notifyResults(Uuid intentId, TransactionResult result);

    void notifyExecutors(Uuid intentId);

    void unregisterForExecutor(IntentTransactionResultListener listener);

    void unregisterForResults(IntentTransactionResultListener resultListener);
}
