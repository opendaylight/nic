/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.transaction.impl;

import org.opendaylight.nic.transaction.api.IntentTransactionListener;
import org.opendaylight.nic.transaction.api.IntentTransactionNotifier;
import org.opendaylight.nic.transaction.TransactionResult;
import org.opendaylight.nic.transaction.api.IntentTransactionRegistryService;
import org.opendaylight.nic.transaction.api.IntentTransactionResultListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yrineu on 26/06/16.
 */
public class IntentTransactionNotifierImpl implements IntentTransactionNotifier {

    private static final Logger LOG = LoggerFactory.getLogger(IntentTransactionNotifierImpl.class);
    private IntentTransactionRegistryService registryService;

    public IntentTransactionNotifierImpl(final IntentTransactionRegistryService registryService) {
        this.registryService = registryService;
    }

    @Override
    public void notifyResults(final Uuid intentId, final TransactionResult result) {
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
    public void notifyExecutors(final Uuid intentId) {
        for (IntentTransactionListener listener : registryService.getExecutorListeners()) {
            listener.executeDeploy(intentId);
        }
    }
}
