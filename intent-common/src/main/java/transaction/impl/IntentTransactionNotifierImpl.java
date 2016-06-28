/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package transaction.impl;

import transaction.TransactionResult;
import transaction.api.IntentTransactionNotifier;
import transaction.api.IntentTransactionRegistryService;
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
    private static IntentTransactionRegistryService registryService;
    protected ServiceRegistration<IntentTransactionNotifier> nicEventServiceRegistration;

    private static IntentTransactionNotifier transactionNotifier;
    private IntentTransactionNotifierImpl() {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        nicEventServiceRegistration = bundleContext.registerService(IntentTransactionNotifier.class, this, null);
        ServiceReference<?> serviceReference = bundleContext
                .getServiceReference(IntentTransactionRegistryService.class);
        registryService = (IntentTransactionRegistryService) bundleContext.getService(serviceReference);
    }

    public static void init() {
        LOG.info("Statrting IntentTransactionNotifyer sevice.");
        if(transactionNotifier == null) {
            transactionNotifier = new IntentTransactionNotifierImpl();
        }
    }

    @Override
    public void notifyResults(Uuid intentId, TransactionResult result) {
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
    public void notifyExecutors(Uuid intentId) {
        for (IntentTransactionListener listener : registryService.getExecutorListeners()) {
            listener.executeDeploy(intentId);
        }
    }
}
