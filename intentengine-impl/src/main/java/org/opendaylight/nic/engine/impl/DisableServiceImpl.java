/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.impl;

import org.opendaylight.nic.api.StateMachineEngineService;
import org.opendaylight.nic.engine.service.DisableService;
import org.opendaylight.nic.impl.StateMachineEngineImpl;
import org.opendaylight.nic.model.MachineState;

public class DisableServiceImpl implements DisableService {

    private StateMachineEngineService engineService;
    private static DisableService disableService;

    private DisableServiceImpl() {
        this.engineService = StateMachineEngineImpl.getInstance();
    }

    public static DisableService getInstance() {
        if(disableService == null) {
            disableService = new DisableServiceImpl();
        }
        return disableService;
    }
    @Override
    public void execute() {
        //TODO: Try to disable Intent
    }

    @Override
    public void onSuccess() {
        engineService.changeState(MachineState.DISABLED);
    }

    @Override
    public void onError() {
        //DO NOTHING
    }
}
