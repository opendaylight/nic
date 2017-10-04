/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.bgp.exception;

import java.io.IOException;

/**
 * Created by yrineu on 10/07/17.
 */
public class BgpRestOperationException extends RuntimeException {

    public BgpRestOperationException(final String message, IOException exception) {
        super("Error when try to evaluate a BGP REST operation. " + message);
    }
}
