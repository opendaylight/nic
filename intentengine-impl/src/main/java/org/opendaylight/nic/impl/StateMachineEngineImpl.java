/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.impl;

import org.opendaylight.nic.api.StateMachineEngineService;
import org.opendaylight.nic.engine.impl.*;
import org.opendaylight.nic.engine.service.EngineService;
import org.opendaylight.nic.model.MachineState;

import java.util.HashMap;
import java.util.Map;

public class StateMachineEngineImpl implements StateMachineEngineService {

    private static StateMachineEngineImpl stateMachineEngine;
    private Map<MachineState, EngineService> engineServiceMap;

    private MachineState currentState;
    private EngineService currentService;

    private StateMachineEngineImpl() {
        engineServiceMap = new HashMap<>();
        currentState = MachineState.UNDEPLOYED;
        populate();
    }

    public static StateMachineEngineImpl getInstance() {
        if(stateMachineEngine == null) {
            stateMachineEngine = new StateMachineEngineImpl();
        }
        return stateMachineEngine;
    }

    public void populate() {
        engineServiceMap.put(MachineState.DEPLOYING, DeployServiceImpl.getInstance());
        engineServiceMap.put(MachineState.DEPLOY_FAILED, DeployFailedServiceImpl.getInstance());
        engineServiceMap.put(MachineState.UNDEPLOYING, UndeployServiceImpl.getInstance());
        engineServiceMap.put(MachineState.UNDEPLOY_FAILED, UndeployFailedServiceImpl.getInstance());
        engineServiceMap.put(MachineState.DISABLING, DisableServiceImpl.getInstance());
    }

    @Override
    public void changeState(MachineState currentState) {
        this.currentState = currentState;

        currentService = engineServiceMap.get(currentState);
        if(currentService != null) {
            currentService.execute();
        }
    }

    @Override
    public MachineState getCurrentStatus() {
        return currentState;
    }

    @Override
    public EngineService getCurrentService() {
        return currentService;
    }
}
