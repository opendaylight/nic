/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.impl;

import org.opendaylight.nic.api.StateMachineEngineService;
import org.opendaylight.nic.engine.service.UndeployService;
import org.opendaylight.nic.impl.StateMachineEngineImpl;
import org.opendaylight.nic.model.MachineState;

public class UndeployServiceImpl implements UndeployService {

    private static UndeployService undeployService;
    private StateMachineEngineService engineService;

    private UndeployServiceImpl() {
        this.engineService = StateMachineEngineImpl.getInstance();
    }

    public static UndeployService getInstance() {
        if(undeployService == null) {
            undeployService = new UndeployServiceImpl();
        }
        return undeployService;
    }

    @Override
    public void execute() {
        //TODO: Verify if can be undeployed
    }

    @Override
    public void onSuccess() {
        engineService.changeState(MachineState.UNDEPLOYED);
    }

    @Override
    public void onError() {
        engineService.changeState(MachineState.UNDEPLOY_FAILED);
    }

    @Override
    public void cancel() {
        engineService.changeState(MachineState.DEPLOYED);
    }
}
