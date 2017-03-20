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
import org.opendaylight.nic.utils.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

public class DeployFailedServiceImpl implements DeployFailedService {

    private static DeployFailedService deployFailedService;
    private static StateMachineEngineService engineService;

    private int retries = 0;

    //TODO: Make it configurable
    private final int MAX_RETRY = 5;

    private DeployFailedServiceImpl(StateMachineEngineService engineService) {
        this.engineService = engineService;

    }

    public static DeployFailedService getInstance(StateMachineEngineService engineService) {
        if (deployFailedService == null) {
            deployFailedService = new DeployFailedServiceImpl(engineService);
        }
        return deployFailedService;
    }

    @Override
    public void execute(final EventType eventType) {
        if (retries < MAX_RETRY) {
            retries++;
            engineService.changeState(Intent.State.DEPLOYING);
        } else {
            cancelRetry();
        }
    }

    public void onSuccess() {
        //DO NOTHING
    }

    public void onError(String message) {
        engineService.changeState(Intent.State.DEPLOYFAILED);
    }

    @Override
    public void cancelRetry() {
        engineService.changeState(Intent.State.DISABLING);
    }
}
