/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.impl;

import org.opendaylight.nic.engine.IntentStateMachineExecutorService;
import org.opendaylight.nic.listeners.api.EventRegistryService;
import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.nic.listeners.api.IEventListener;
import org.opendaylight.nic.listeners.api.IntentAdded;
import org.opendaylight.nic.listeners.api.IntentUpdated;
import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.nic.listeners.api.NodeDeleted;
import org.opendaylight.nic.listeners.api.NodeUp;
import org.opendaylight.nic.listeners.api.NodeUpdated;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateMachineEventsHandler implements IEventListener{

    private EventRegistryService eventRegistryService;
    private Map<Class, EventExecutor> eventExecutorMap;
    private IntentStateMachineExecutorService stateMachineExecutorService;

    private abstract interface EventExecutor<T extends NicNotification> {
        void execute(T event);
    }

    public StateMachineEventsHandler() {
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<?> serviceReference = bundleContext.getServiceReference(EventRegistryService.class);
        eventRegistryService = (EventRegistryService) bundleContext.getService(serviceReference);
        eventExecutorMap = new HashMap<>();
        stateMachineExecutorService = new IntentStateMachineExecutor();
        populateEventListener(EventType.INTENT_ADDED,
                EventType.INTENT_REMOVED,
                EventType.INTENT_UPDATE,
                EventType.NODE_ADDED,
                EventType.NODE_REMOVED,
                EventType.NODE_UPDATED);
        createEventExecutors();
    }

    @Override
    public void handleEvent(NicNotification event) {
        EventExecutor eventExecutor = eventExecutorMap.get(event);
        if (eventExecutor != null) {
            eventExecutorMap.get(event).execute(event);
        }
    }

    private void populateEventListener(EventType ... eventTypes) {
        for(EventType eventType : eventTypes) {
            eventRegistryService.registerEventListener(eventType, this);
        }
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
                stateMachineExecutorService.createTransaction(event.getIntent(), ReceivedEvent.INTENT_UPDATED);
            }
        };
    }

    private EventExecutor getIntentUpdatedExecutor() {
        return new EventExecutor<IntentUpdated>() {
            @Override
            public void execute(IntentUpdated event) {
                final Intent intent = event.getIntent();
                stateMachineExecutorService.removeTransactions(intent.getId(), ReceivedEvent.INTENT_UPDATED);
                stateMachineExecutorService.createTransaction(intent, ReceivedEvent.INTENT_UPDATED);
            }
        };
    }

    private EventExecutor getNodeUpExecutor() {
        return new EventExecutor<NodeUp>() {
            @Override
            public void execute(NodeUp event) {
                executeNodeEvent(event.getIp(), ReceivedEvent.NODE_UP);
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

    private void executeNodeEvent(IpAddress ipAddress, ReceivedEvent eventType) {
        List<Intent> intents = stateMachineExecutorService.getUndeployedIntents(ipAddress);
        for(Intent intent : intents) {
            stateMachineExecutorService.createTransaction(intent, eventType);
        }
    }
}
