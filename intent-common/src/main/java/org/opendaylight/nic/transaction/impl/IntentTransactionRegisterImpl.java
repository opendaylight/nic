/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.transaction.impl;

import org.opendaylight.nic.transaction.api.IntentTransactionListener;
import org.opendaylight.nic.transaction.api.IntentTransactionResultListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.nic.transaction.api.IntentTransactionRegistryService;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by yrineu on 25/06/16.
 */
public class IntentTransactionRegisterImpl implements IntentTransactionRegistryService {

    private static final Logger LOG = LoggerFactory.getLogger(IntentTransactionRegisterImpl.class);
    protected static ServiceRegistration<IntentTransactionRegistryService> nicEventServiceRegistration;

    private static IntentTransactionRegistryService registryService;
    private Set<IntentTransactionListener> transactionListenerSet;
    private Set<IntentTransactionResultListener> transactionResultListeners;

    private IntentTransactionRegisterImpl() {
        transactionListenerSet = new HashSet<>();
        transactionResultListeners = new HashSet<>();
    }

    public static void init() {
        LOG.info("Starting IntentTransactionRegister service.");
        if(registryService == null) {
            registryService = new IntentTransactionRegisterImpl();
            BundleContext bundleContext = FrameworkUtil.getBundle(registryService.getClass()).getBundleContext();
            nicEventServiceRegistration = bundleContext.registerService(IntentTransactionRegistryService.class, registryService, null);
        }
    }

    public void registerForExecutor(IntentTransactionListener listener) {
        transactionListenerSet.add(listener);
    }

    public void registerForResults(IntentTransactionResultListener listener) {
        transactionResultListeners.add(listener);
    }

    @Override
    public void unregisterForExecutor(IntentTransactionResultListener listener) {
        transactionListenerSet.remove(listener);
    }

    @Override
    public void unregisterForResults(IntentTransactionListener listener) {
        transactionResultListeners.remove(listener);
    }

    @Override
    public Set<IntentTransactionListener> getExecutorListeners() {
        return this.transactionListenerSet;
    }

    @Override
    public Set<IntentTransactionResultListener> getResulListeners() {
        return this.transactionResultListeners;
    }
}
