/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.impl;

import org.opendaylight.nic.engine.StateMachineEngineService;
import org.opendaylight.nic.engine.service.UndeployService;
import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

public class UndeployServiceImpl implements UndeployService {

    private static UndeployService undeployService;
    private StateMachineEngineService engineService;

    private UndeployServiceImpl(StateMachineEngineService engineService) {
        this.engineService = engineService;
    }

    public static UndeployService getInstance(StateMachineEngineService engineService) {
        if(undeployService == null) {
            undeployService = new UndeployServiceImpl(engineService);
        }
        return undeployService;
    }

    @Override
    public void execute(EventType eventType) {
        //TODO: Create an async call to undeploy Intent
    }

    @Override
    public void onSuccess() {
        engineService.changeState(Intent.State.UNDEPLOYED);
    }

    @Override
    public void onError() {
        engineService.changeState(Intent.State.UNDEPLOYFAILED);
    }

    @Override
    public void cancel() {
        engineService.changeState(Intent.State.DEPLOYED);
    }
}
