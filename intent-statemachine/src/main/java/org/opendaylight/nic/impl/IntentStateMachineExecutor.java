/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.impl;

import org.opendaylight.nic.engine.IntentStateMachineExecutorService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

import java.util.List;

//TODO: In the future, this class must use the MD-SAL to execute transactions
public class IntentStateMachineExecutor implements IntentStateMachineExecutorService {

    @Override
    public void createTransaction(Intent intent, ReceivedEvent receivedEvent) {

    }

    @Override
    public void removeTransactions(Uuid intentId, ReceivedEvent receivedEvent) {
        //TODO: Use the queue on MD-SAL to remove ready transactions
        //tagged with this intentId
    }

    @Override
    public List<Intent> getUndeployedIntents(IpAddress ipAddress) {
        //TODO: Return all Undeployed Intents from the queue that does match with this IPAddress
        return null;
    }
}
