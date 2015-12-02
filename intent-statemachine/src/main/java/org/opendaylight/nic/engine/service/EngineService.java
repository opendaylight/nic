/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.service;

/**
 * Service to handle State Machine behaviors and events
 */
public interface EngineService {

    /**
     * Starts State Machine execution
     */
    void execute();

    /**
     * Handle State Machine execution success
     */
    void onSuccess();

    /**
     * Handle State Machine in case of errors when engine execution
     */
    void onError();
}
