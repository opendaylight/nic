/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.impl;

import org.opendaylight.nic.engine.StateMachineEngineService;
import org.opendaylight.nic.engine.service.DisableService;
import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

public class DisableServiceImpl implements DisableService {

    private StateMachineEngineService engineService;
    private static DisableService disableService;

    private DisableServiceImpl(StateMachineEngineService engineService) {
        this.engineService = engineService;
    }

    public static DisableService getInstance(StateMachineEngineService engineService) {
        if(disableService == null) {
            disableService = new DisableServiceImpl(engineService);
        }
        return disableService;
    }
    @Override
    public void execute(EventType eventType) {
        //TODO: Try to disable Intent
    }

    @Override
    public void onSuccess() {
        engineService.changeState(Intent.State.DISABLED);
    }

    @Override
    public void onError() {
        //DO NOTHING
    }
}
