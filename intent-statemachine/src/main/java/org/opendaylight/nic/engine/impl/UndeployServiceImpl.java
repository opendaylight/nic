/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.engine.impl;

import org.opendaylight.nic.engine.StateMachineEngineService;
import org.opendaylight.nic.engine.service.StateMachineRendererService;
import org.opendaylight.nic.engine.service.UndeployService;
import org.opendaylight.nic.impl.StateMachineRendererExecutor;
import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UndeployServiceImpl implements UndeployService {

    private static final Logger LOG = LoggerFactory.getLogger(UndeployServiceImpl.class);
    private static UndeployService undeployService;
    private static StateMachineEngineService engineService;
    private static StateMachineRendererService rendererService;

    private UndeployServiceImpl(StateMachineEngineService engineService) {
        this.engineService = engineService;
        rendererService = new StateMachineRendererExecutor(this);
    }

    public static UndeployService getInstance(StateMachineEngineService engineService) {
        if(undeployService == null) {
            undeployService = new UndeployServiceImpl(engineService);
        }
        return undeployService;
    }

    @Override
    public void execute(EventType eventType) {
        rendererService.undeploy();
    }

    @Override
    public void onSuccess() {
        engineService.changeState(Intent.State.UNDEPLOYED);
    }

    @Override
    public void onError(String message) {
        LOG.error(message);
        engineService.changeState(Intent.State.UNDEPLOYFAILED);
    }

    @Override
    public void cancel() {
        engineService.changeState(Intent.State.DEPLOYED);
    }
}
