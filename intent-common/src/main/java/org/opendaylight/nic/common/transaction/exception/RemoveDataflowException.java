/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.exception;

/**
 * Created by yrineu on 28/03/17.
 */
public class RemoveDataflowException extends Exception {

    private static final String ERROR_MESSAGE = "Error when try to remove dataflow: ";

    public RemoveDataflowException(final Exception exception) {
        super(ERROR_MESSAGE + exception.getCause().getMessage());
    }
}
