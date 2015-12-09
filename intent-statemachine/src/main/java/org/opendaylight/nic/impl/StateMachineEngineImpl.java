/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.impl;

import org.opendaylight.nic.engine.StateMachineEngineService;
import org.opendaylight.nic.engine.impl.DeployFailedServiceImpl;
import org.opendaylight.nic.engine.impl.DeployServiceImpl;
import org.opendaylight.nic.engine.impl.DisableServiceImpl;
import org.opendaylight.nic.engine.impl.UndeployFailedServiceImpl;
import org.opendaylight.nic.engine.impl.UndeployServiceImpl;
import org.opendaylight.nic.engine.service.EngineService;
import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

import java.util.HashMap;
import java.util.Map;

public class StateMachineEngineImpl implements StateMachineEngineService {

    private static Map<Intent.State, EngineService> engineServiceMap;

    private static EventType eventType;

    private static StateMachineEngineService engineService;

    public StateMachineEngineImpl() {
        engineService = this;
        engineServiceMap = new HashMap<>();
        populate();
    }

    private void populate() {
        engineServiceMap.put(Intent.State.DEPLOYING, DeployServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.DEPLOYFAILED, DeployFailedServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.UNDEPLOYING, UndeployServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.UNDEPLOYFAILED, UndeployFailedServiceImpl.getInstance(engineService));
        engineServiceMap.put(Intent.State.DISABLING, DisableServiceImpl.getInstance(engineService));
    }

    public void changeState(final Intent.State currentState) {
        //TODO: Change intent state on MD-SAL
        final EngineService currentService = engineServiceMap.get(currentState);
        if(currentService != null) {
            currentService.execute(eventType);
        }
    }

    @Override
    public void execute(Intent.State state, EventType eventType) {
        this.eventType = eventType;
        changeState(state);
    }
}
