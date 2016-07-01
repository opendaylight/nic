/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.transaction.api;

import java.util.Set;

/**
 * Created by yrineu on 26/06/16.
 */
public interface IntentTransactionRegistryService {

    void registerForExecutor(IntentTransactionListener listener);

    void registerForResults(IntentTransactionResultListener listener);

    void unregisterForExecutor(IntentTransactionResultListener listener);

    void unregisterForResults(IntentTransactionListener listener);

    Set<IntentTransactionListener> getExecutorListeners();

    Set<IntentTransactionResultListener> getResulListeners();
}
