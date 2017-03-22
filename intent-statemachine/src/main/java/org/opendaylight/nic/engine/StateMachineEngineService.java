/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine;


import org.opendaylight.nic.utils.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.intent.state.transactions.IntentStateTransaction;

import java.util.concurrent.ExecutionException;

/**
 * Service to handle state changes
 */
public interface StateMachineEngineService {

    /**
     * Verify if can execute action based on remaining attempts
     * @param id the Intent ID as {@link String}
     * @param eventType the {@link EventType}
     * @return a {@link Boolean} value
     */
    boolean canExecute(String id, EventType eventType);
}
