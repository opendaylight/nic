/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.impl;

import org.opendaylight.nic.engine.StateMachineEngineService;
import org.opendaylight.nic.engine.service.DeployFailedService;
import org.opendaylight.nic.engine.service.StateMachineRendererService;
import org.opendaylight.nic.impl.StateMachineRendererExecutor;
import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

public class DeployFailedServiceImpl implements DeployFailedService {

    private static DeployFailedService deployFailedService;
    private static StateMachineEngineService engineService;
    private static StateMachineRendererService rendererService;

    private int retries = 0;

    //TODO: Make it configurable
    private final int MAX_RETRY = 5;

    private DeployFailedServiceImpl(StateMachineEngineService engineService) {
        this.engineService = engineService;
        rendererService = new StateMachineRendererExecutor(this);

    }

    public static DeployFailedService getInstance(StateMachineEngineService engineService) {
        if (deployFailedService == null) {
            deployFailedService = new DeployFailedServiceImpl(engineService);
        }
        return deployFailedService;
    }

    @Override
    public void execute(EventType eventType) {
        if (retries < MAX_RETRY) {
            retries++;
            engineService.changeState(Intent.State.DEPLOYING);
        } else {
            cancelRetry();
        }
    }

    @Override
    public void onSuccess() {
        //DO NOTHING
    }

    @Override
    public void onError(String message) {
        engineService.changeState(Intent.State.DEPLOYFAILED);
    }

    @Override
    public void cancelRetry() {
        engineService.changeState(Intent.State.DISABLING);
    }
}
