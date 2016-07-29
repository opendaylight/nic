/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.transaction.impl;

import org.opendaylight.nic.transaction.TransactionResult;
import org.opendaylight.nic.transaction.api.IntentCommonProviderService;
import org.opendaylight.nic.transaction.api.IntentTransactionListener;
import org.opendaylight.nic.transaction.api.IntentTransactionNotifier;
import org.opendaylight.nic.transaction.api.IntentTransactionRegistryService;
import org.opendaylight.nic.transaction.api.IntentTransactionResultListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * Created by yrineu on 30/06/16.
 */
public class IntentCommonProviderServiceImpl implements IntentCommonProviderService {

    final IntentTransactionRegistryService registryService;
    final IntentTransactionNotifier notifierService;
    protected BundleContext context;

    public IntentCommonProviderServiceImpl() {
        registryService = new IntentTransactionRegisterImpl();
        notifierService = new IntentTransactionNotifierImpl(registryService);
    }

    @Override
    public void start() {
        context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        context.registerService(IntentCommonProviderService.class, this, null);
    }

    @Override
    public void registerForExecutor(IntentTransactionListener transactionListener) {
        registryService.registerForExecutor(transactionListener);
    }

    @Override
    public void registerForResults(IntentTransactionResultListener resultListener) {
        registryService.registerForResults(resultListener);
    }

    @Override
    public void notifyResults(Uuid intentId, TransactionResult result) {
        notifierService.notifyResults(intentId, result);
    }

    @Override
    public void notifyExecutors(Uuid intentId) {
        notifierService.notifyExecutors(intentId);
    }

    @Override
    public void unregisterForExecutor(IntentTransactionResultListener listener) {
        registryService.unregisterForExecutor(listener);
    }

    @Override
    public void unregisterForResults(IntentTransactionResultListener resultListener) {
        registryService.unregisterForResults(resultListener);
    }

    @Override
    public void close() throws Exception {
    }
}
