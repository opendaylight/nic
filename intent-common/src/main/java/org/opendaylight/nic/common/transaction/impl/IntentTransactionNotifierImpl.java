/*
 * Copyright (c) 2016 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.impl;

import org.opendaylight.nic.common.transaction.TransactionResult;
import org.opendaylight.nic.common.transaction.api.IntentTransactionListener;
import org.opendaylight.nic.common.transaction.api.IntentTransactionNotifier;
import org.opendaylight.nic.common.transaction.api.IntentTransactionRegistryService;
import org.opendaylight.nic.common.transaction.api.IntentTransactionResultListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntentTransactionNotifierImpl implements IntentTransactionNotifier {

    private static final Logger LOG = LoggerFactory.getLogger(IntentTransactionNotifierImpl.class);
    private IntentTransactionRegistryService registryService;

    public IntentTransactionNotifierImpl(final IntentTransactionRegistryService registryService) {
        this.registryService = registryService;
    }

    @Override
    public void notifyResults(final String intentId, final TransactionResult result) {
        for (IntentTransactionResultListener listener : registryService.getResulListeners()) {
            switch (result) {
                case SUCCESS:
                    listener.deploySuccess(intentId);
                    break;
                case FAILURE:
                    listener.deployFailure(intentId);
                    break;
                default:
                    listener.deployFailure(intentId);
            }
        }
    }

    @Override
    public void notifyExecutors(final String intentId) {
        for (IntentTransactionListener listener : registryService.getExecutorListeners()) {
            listener.executeDeploy(intentId);
        }
    }
}
