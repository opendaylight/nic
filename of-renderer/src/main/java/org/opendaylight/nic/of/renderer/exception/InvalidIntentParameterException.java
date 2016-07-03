/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.exception;

import java.util.NoSuchElementException;

/**
 * Created by yrineu on 02/06/16.
 */
public class InvalidIntentParameterException extends NoSuchElementException {

    private static final String ERROR_MESSAGE = "Error when try to use an invalid parameter: ";

    public InvalidIntentParameterException(String message) {
        super(ERROR_MESSAGE + message);
    }
}
