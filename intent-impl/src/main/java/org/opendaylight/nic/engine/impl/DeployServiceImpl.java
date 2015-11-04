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
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

public class DeployServiceImpl implements DeployService {

    private StateMachineEngineService engineService;
    private static DeployService deployService;

    private DeployServiceImpl(StateMachineEngineService engineService) {
        this.engineService = engineService;
    }

    public static DeployService getInstance(StateMachineEngineService engineService) {
        if(deployService == null) {
            deployService = new DeployServiceImpl(engineService);
        }
        return deployService;
    }

    @Override
    public void execute() {
        //TODO: Execute a sync call to OFRenderer
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
}
