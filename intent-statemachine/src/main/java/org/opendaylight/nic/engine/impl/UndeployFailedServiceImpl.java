/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.impl;

import org.opendaylight.nic.engine.StateMachineEngineService;
import org.opendaylight.nic.engine.service.UndeployFailedService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

public class UndeployFailedServiceImpl implements UndeployFailedService {

    private StateMachineEngineService engineService;
    private static UndeployFailedService undeployFailedService;

    private UndeployFailedServiceImpl(StateMachineEngineService engineService) {
        this.engineService = engineService;
    }

    public static UndeployFailedService getInstance(StateMachineEngineService engineService) {
        if(undeployFailedService == null) {
            undeployFailedService = new UndeployFailedServiceImpl(engineService);
        }
        return undeployFailedService;
    }

    @Override
    public void execute() {
        //TODO: Retry undeploy
        engineService.changeState(Intent.State.UNDEPLOYING);
    }

    @Override
    public void onSuccess() {
        //DO NOTHING
    }

    @Override
    public void onError() {
        //DO NOTHING
    }

    @Override
    public void cancelRetry() {
        engineService.changeState(Intent.State.DISABLING);
    }
}