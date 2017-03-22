/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.api;

import org.opendaylight.nic.common.transaction.TransactionResult;

public interface IntentCommonProviderService extends AutoCloseable {

    void start();

    IntentCommonService retrieveCommonServiceInstance();
}
