/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.exception;

/**
 * Created by yrineu on 17/07/17.
 */
public class JuniperRPCException extends NoSuchFieldException {

    public JuniperRPCException(final String message) {
        super("Error when try to evaluate a RPC call over HTTP request for a Juniper device "+message);
    }
}
