/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.impl;

/**
 * RuntimeException for Intent State Machine transactions error.
 */
public class StateMachineException extends RuntimeException {

    private static final String INTERNAL_MESSAGE = "Error when execute state transaction: ";
    public StateMachineException(String message) {
        super(INTERNAL_MESSAGE + message);
    }

    private StateMachineException() {
    }
}
