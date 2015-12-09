/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine;


import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

/**
 * Service to handle state changes
 */
public interface StateMachineEngineService {

    /**
     * Execute State Machine
     * @param state
     * @param eventType
     */
    void execute(Intent.State state, EventType eventType);
    /**
     * Change current state to a given state
     * @param currentState
     */
    void changeState(Intent.State currentState);
}
