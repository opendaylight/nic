/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.exception;

import java.util.concurrent.ExecutionException;

/**
 * Created by yrineu on 07/04/17.
 */
public class RendererServiceException extends ExecutionException {

    private static final String ERROR_MESSAGE = "Error when try to start a renderer action: ";

    public RendererServiceException(final String message) {
        super(ERROR_MESSAGE + message);
    }
}
