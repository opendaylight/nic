/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.engine.IntentStateMachineExecutorService;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.IEventService;
import org.opendaylight.nic.listeners.api.IntentAdded;
import org.opendaylight.nic.listeners.api.IntentUpdated;
import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.nic.listeners.api.NodeDeleted;
import org.opendaylight.nic.listeners.api.NodeUp;
import org.opendaylight.nic.listeners.api.NodeUpdated;
import org.opendaylight.nic.listeners.impl.IntentNotificationSupplierImpl;
import org.opendaylight.nic.listeners.impl.NodeNotificationSupplierImpl;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateMachineEventsHandler implements IEventListener{

    private IEventService intentNotificationService;
    private IEventService nodeNotificationService;
    private Map<Class, EventExecutor> eventExecutorMap;
    private IntentStateMachineExecutorService stateMachineExecutorService;

    private abstract interface EventExecutor<T extends NicNotification> {
        void execute(T event);
    }

    public StateMachineEventsHandler(DataBroker dataBroker) {
        eventExecutorMap = new HashMap<>();
        intentNotificationService = new IntentNotificationSupplierImpl(dataBroker);
        nodeNotificationService = new NodeNotificationSupplierImpl(dataBroker);
        stateMachineExecutorService = new IntentStateMachineExecutor();
        intentNotificationService.addEventListener(this);
        nodeNotificationService.addEventListener(this);

        createEventExecutors();
    }

    @Override
    public void handleEvent(NicNotification event) {
        eventExecutorMap.get(event).execute(event);
    }

    private void createEventExecutors() {
        eventExecutorMap.put(IntentAdded.class, getIntentAddedExecutor());
        eventExecutorMap.put(IntentUpdated.class, getIntentUpdatedExecutor());
        eventExecutorMap.put(NodeUp.class, getNodeUpExecutor());
        eventExecutorMap.put(NodeUpdated.class, getNodeUpdatedExecutor());
        eventExecutorMap.put(NodeDeleted.class, getNodeDeletedExecutor());
    }

    private EventExecutor getIntentAddedExecutor() {
        return new EventExecutor<IntentAdded>() {
            @Override
            public void execute(IntentAdded event) {
                stateMachineExecutorService.createTransaction(event.getIntent(),EventType.INTENT_CREATED);
            }
        };
    }

    private EventExecutor getIntentUpdatedExecutor() {
        return new EventExecutor<IntentUpdated>() {
            @Override
            public void execute(IntentUpdated event) {
                final Intent intent = event.getIntent();
                stateMachineExecutorService.removeTransactions(intent.getId(), EventType.INTENT_UPDATED);
                stateMachineExecutorService.createTransaction(intent, EventType.INTENT_UPDATED);
            }
        };
    }

    private EventExecutor getNodeUpExecutor() {
        return new EventExecutor<NodeUp>() {
            @Override
            public void execute(NodeUp event) {
                executeNodeEvent(event.getIp(), EventType.NODE_UP);
            }
        };
    }

    private EventExecutor getNodeUpdatedExecutor() {
        return new EventExecutor<NodeUpdated>() {
            @Override
            public void execute(NodeUpdated event) {
                //TODO: Implement a behavior for NodeUpdate event
            }
        };
    }

    private EventExecutor getNodeDeletedExecutor() {
        return new EventExecutor<NodeDeleted>() {
            @Override
            public void execute(NodeDeleted event) {
                //TODO: Implement a behavior for NodeDeleted event
            }
        };
    }

    private void executeNodeEvent(IpAddress ipAddress, EventType eventType) {
        List<Intent> intents = stateMachineExecutorService.getUndeployedIntents(ipAddress);
        for(Intent intent : intents) {
            stateMachineExecutorService.createTransaction(intent, eventType);
        }
    }
}
