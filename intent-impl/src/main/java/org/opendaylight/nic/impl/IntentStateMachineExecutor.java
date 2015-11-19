package org.opendaylight.nic.impl;

import org.opendaylight.nic.engine.IntentStateMachineExecutorService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;

import java.util.List;

//TODO: In the future, this class must use the MD-SAL to execute transactions
public class IntentStateMachineExecutor implements IntentStateMachineExecutorService{

    @Override
    public void createTransaction(Intent intent, EventType eventType) {

    }

    @Override
    public void removeTransactions(Uuid intentId, EventType eventType) {
        //TODO: Use the queue on MD-SAL to remove ready transactions
        //tagged with this intentId
    }

    @Override
    public List<Intent> getUndeployedIntents(IpAddress ipAddress) {
        //TODO: Return all Undeployed Intents from the queue that does match with this IPAddress
        return null;
    }
}
