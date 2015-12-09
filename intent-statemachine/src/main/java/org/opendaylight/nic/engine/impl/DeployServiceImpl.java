/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.impl;

import org.opendaylight.nic.engine.StateMachineEngineService;
import org.opendaylight.nic.engine.service.DeployService;
import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

import java.util.HashMap;
import java.util.Map;

public class DeployServiceImpl implements DeployService {

    private interface DeployExecutor {
        void execute();
    }

    private Map<Intent.State, DeployExecutor> executorMap;
    private static StateMachineEngineService engineService;
    private static DeployService deployService;

    private DeployServiceImpl(StateMachineEngineService engineService) {
        executorMap = new HashMap<>();
        populateExecutorMap();
        this.engineService = engineService;
    }

    private void populateExecutorMap() {
        executorMap.put(Intent.State.DEPLOYING, new DeployExecutor() {
            @Override
            public void execute() {
                //TODO: Create an async call to RENDERER for intent deploy
                onSuccess();
            }
        });

        executorMap.put(Intent.State.UNDEPLOYED, new DeployExecutor() {
            @Override
            public void execute() {
                cancel();
            }
        });
    }

    public static DeployService getInstance(StateMachineEngineService engineService) {
        if(deployService == null) {
            deployService = new DeployServiceImpl(engineService);
        }
        return deployService;
    }

    @Override
    public void execute(final EventType eventType) {
        DeployExecutor deployExecutor = executorMap.get(getNextState(eventType));
        deployExecutor.execute();
    }

    @Override
    public void onSuccess() {
        engineService.changeState(Intent.State.DEPLOYED);
    }

    @Override
    public void onError() {
        engineService.changeState(Intent.State.DEPLOYFAILED);
    }

    @Override
    public void cancel() {
        engineService.changeState(Intent.State.UNDEPLOYED);
    }

    public Intent.State getNextState(EventType eventType) {
        Intent.State result;
        switch (eventType) {
            case NODE_ADDED:
                result = Intent.State.DEPLOYING;
                break;
            case NODE_REMOVED:
                result = Intent.State.UNDEPLOYED;
                break;
            default:
                result = Intent.State.DEPLOYING;
                break;
        }
        return result;
    }
}
