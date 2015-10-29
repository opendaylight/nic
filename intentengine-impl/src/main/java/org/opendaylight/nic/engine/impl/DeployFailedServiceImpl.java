/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.impl;

import org.opendaylight.nic.api.StateMachineEngineService;
import org.opendaylight.nic.engine.service.DeployFailedService;
import org.opendaylight.nic.impl.StateMachineEngineImpl;
import org.opendaylight.nic.model.MachineState;

public class DeployFailedServiceImpl implements DeployFailedService {

    private static DeployFailedService deployFailedService;
    private StateMachineEngineService engineService;

    private DeployFailedServiceImpl() {
        this.engineService = StateMachineEngineImpl.getInstance();
    }

    public static DeployFailedService getInstance() {
        if (deployFailedService == null) {
           deployFailedService = new DeployFailedServiceImpl();
        }
        return deployFailedService;
    }

    @Override
    public void execute() {
        //TODO: Retry
        engineService.changeState(MachineState.DEPLOYING);
    }

    @Override
    public void onSuccess() {
        //DO NOTHING
    }

    @Override
    public void onError() {
        engineService.changeState(MachineState.DEPLOY_FAILED);
    }

    @Override
    public void cancelRetry() {
        engineService.changeState(MachineState.DISABLING);
    }
}
