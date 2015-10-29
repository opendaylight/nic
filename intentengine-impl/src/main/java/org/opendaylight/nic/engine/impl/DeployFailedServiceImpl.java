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
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

public class DeployFailedServiceImpl implements DeployFailedService {

    private static DeployFailedService deployFailedService;
    private StateMachineEngineService engineService;

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
    public void execute() {
        //TODO: Retry
        engineService.changeState(Intent.State.DEPLOYING);
    }

    @Override
    public void onSuccess() {
        //DO NOTHING
    }

    @Override
    public void onError() {
        engineService.changeState(Intent.State.DEPLOYFAILED);
    }

    @Override
    public void cancelRetry() {
        engineService.changeState(Intent.State.DISABLING);
    }
}
