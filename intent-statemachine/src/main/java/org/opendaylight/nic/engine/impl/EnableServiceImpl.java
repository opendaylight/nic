/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.impl;

import org.opendaylight.nic.engine.StateMachineEngineService;
import org.opendaylight.nic.engine.service.EnableService;
import org.opendaylight.nic.engine.service.StateMachineRendererService;
import org.opendaylight.nic.impl.StateMachineRendererExecutor;
import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;

public class EnableServiceImpl implements EnableService {

    private StateMachineEngineService engineService;
    private static EnableService enableService;

    private EnableServiceImpl(StateMachineEngineService engineService) {
        this.engineService = engineService;
    }

    public static EnableService getInstance(StateMachineEngineService engineService) {
        if(enableService == null) {
            enableService = new EnableServiceImpl(engineService);
        }
        return enableService;
    }

    @Override
    public void execute(EventType eventType) {
        engineService.changeState(Intent.State.UNDEPLOYED);
    }

    @Override
    public void onSuccess() {
        //DO NOTHING
    }

    @Override
    public void onError(String message) {
        //DO NOTHING
    }
}
