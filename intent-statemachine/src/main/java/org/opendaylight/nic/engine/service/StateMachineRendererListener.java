/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.engine.service;

/**
 * Created by yrineu on 28/12/15.
 */
public interface StateMachineRendererListener {

    /**
     * Handle State Machine execution success
     */
    void onSuccess();

    /**
     * Handle State Machine in case of errors when engine execution
     */
    void onError(String cause);
}
