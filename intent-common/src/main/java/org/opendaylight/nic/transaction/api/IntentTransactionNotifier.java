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
import org.osgi.framework.BundleContext;

/**
 * Created by yrineu on 26/06/16.
 */
public interface IntentTransactionNotifier {

    void notifyResults(Uuid intentId, TransactionResult result);

    void notifyExecutors(Uuid intentId);
}
