/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.transaction.impl;

/**
 * Created by yrineu on 30/06/16.
 */
public class IntentCommonProviderImpl implements AutoCloseable {

    public IntentCommonProviderImpl() {
    }

    public void start() {
        IntentTransactionRegisterImpl.init();
        IntentTransactionNotifierImpl.init();
    }
    @Override
    public void close() throws Exception {

    }
}
