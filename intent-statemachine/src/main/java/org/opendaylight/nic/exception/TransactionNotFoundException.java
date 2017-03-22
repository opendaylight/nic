/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.exception;

import java.util.NoSuchElementException;

/**
 * Created by yrineu on 28/03/17.
 */
public class TransactionNotFoundException extends NoSuchElementException {

    private static final String ERROR_MESSAGE = "Transaction not found: ";

    public TransactionNotFoundException(String message) {
        super(ERROR_MESSAGE + message);
    }
}
