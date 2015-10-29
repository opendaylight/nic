/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.impl;

import org.opendaylight.nic.api.StateMachineEngineService;
import org.opendaylight.nic.engine.service.DeployService;
import org.opendaylight.nic.impl.StateMachineEngineImpl;
import org.opendaylight.nic.model.MachineState;

public class DeployServiceImpl implements DeployService {

    private StateMachineEngineService engineService;
    private static DeployService deployService;

    private DeployServiceImpl() {
        this.engineService = StateMachineEngineImpl.getInstance();
    }

    public static DeployService getInstance() {
        if(deployService == null) {
            deployService = new DeployServiceImpl();
        }
        return deployService;
    }

    @Override
    public void execute() {
        //TODO: Execute a sync call to OFRenderer
    }

    @Override
    public void onSuccess() {
        engineService.changeState(MachineState.DEPLOYED);
    }

    @Override
    public void onError() {
        engineService.changeState(MachineState.DEPLOY_FAILED);
    }

    @Override
    public void cancel() {
        engineService.changeState(MachineState.UNDEPLOYED);
    }
}
