/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.impl;

import org.opendaylight.nic.api.StateMachineEngineService;
import org.opendaylight.nic.engine.service.UndeployFailedService;
import org.opendaylight.nic.impl.StateMachineEngineImpl;
import org.opendaylight.nic.model.MachineState;

public class UndeployFailedServiceImpl implements UndeployFailedService {

    private StateMachineEngineService engineService;
    private static UndeployFailedService undeployFailedService;

    private UndeployFailedServiceImpl() {
        this.engineService = StateMachineEngineImpl.getInstance();
    }

    public static UndeployFailedService getInstance() {
        if(undeployFailedService == null) {
            undeployFailedService = new UndeployFailedServiceImpl();
        }
        return undeployFailedService;
    }

    @Override
    public void execute() {
        //TODO: Retry undeploy
        engineService.changeState(MachineState.UNDEPLOYING);
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
        engineService.changeState(MachineState.DISABLING);
    }
}