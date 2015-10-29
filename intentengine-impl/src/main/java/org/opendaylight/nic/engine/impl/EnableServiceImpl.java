/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.impl;

import org.opendaylight.nic.api.StateMachineEngineService;
import org.opendaylight.nic.engine.service.EnableService;
import org.opendaylight.nic.impl.StateMachineEngineImpl;
import org.opendaylight.nic.model.MachineState;

public class EnableServiceImpl implements EnableService {

    private StateMachineEngineService engineService;
    private static EnableService enableService;

    private EnableServiceImpl() {
        this.engineService = StateMachineEngineImpl.getInstance();
    }

    public static EnableService getInstance() {
        if(enableService == null) {
            enableService = new EnableServiceImpl();
        }
        return enableService;
    }

    @Override
    public void execute() {
        engineService.changeState(MachineState.UNDEPLOYED);
    }

    @Override
    public void onSuccess() {
        //DO NOTHING
    }

    @Override
    public void onError() {
        //DO NOTHING
    }
}
