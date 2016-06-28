/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.impl;

import org.opendaylight.nic.engine.StateMachineEngineService;
import org.opendaylight.nic.engine.service.UndeployedService;
import transaction.api.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yrineu on 27/06/16.
 */
public class UndeployedServiceImpl implements UndeployedService {

    private static final Logger LOG = LoggerFactory.getLogger(UndeployedServiceImpl.class);
    private static StateMachineEngineService engineService;
    private static UndeployedService undeployedService;

    private UndeployedServiceImpl(StateMachineEngineService engineService) {
        this.engineService = engineService;
    }

    public static UndeployedService getInstance(StateMachineEngineService engineService) {
        if(undeployedService == null) {
            undeployedService = new UndeployedServiceImpl(engineService);
        }
        return undeployedService;
    }

    @Override
    public void execute(EventType eventType) {
        executeByEvent(eventType);
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError(String cause) {

    }

    private void executeByEvent(EventType eventType) {
        switch (eventType) {
            case INTENT_ADDED:
            case NODE_ADDED:
                engineService.changeState(Intent.State.DEPLOYING);
                break;
        }
    }
}
