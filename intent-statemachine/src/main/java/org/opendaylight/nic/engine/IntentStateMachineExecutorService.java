/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine;

import org.opendaylight.nic.impl.ReceivedEvent;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

import java.util.List;

/**
 * Service to handle State Machine transactions
 */
public interface IntentStateMachineExecutorService {

    /**
     * Create a new transaction for a given Intent based in a given event
     * @param intent
     * @param receivedEvent Event received by event-listener
     */
    void createTransaction(Intent intent, ReceivedEvent receivedEvent);

    /**
     * Remove a transaction for a given Intent
     * @param intentId
     * @param receivedEvent
     */
    void removeTransactions(Uuid intentId, ReceivedEvent receivedEvent);

    /**
     * Retrieve all undeployed Intents for a given IpAddress
     * @param ipAddress
     * @return
     */
    List<Intent> getUndeployedIntents(IpAddress ipAddress);
}
