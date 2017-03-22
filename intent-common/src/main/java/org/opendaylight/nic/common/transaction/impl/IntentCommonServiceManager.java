/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.common.transaction.api.IntentCommonService;
import org.opendaylight.nic.common.transaction.service.lifecycle.IntentLifeCycleService;
import org.opendaylight.nic.common.transaction.service.renderer.IntentActionFactory;
import org.opendaylight.nic.common.transaction.service.renderer.OFRendererService;
import org.opendaylight.nic.common.transaction.utils.CommonUtils;
import org.opendaylight.nic.engine.IntentStateMachineExecutorService;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.utils.EventType;
import org.opendaylight.schedule.ScheduleService;
import org.opendaylight.schedule.ScheduleServiceManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yrineu on 10/04/17.
 */
public class IntentCommonServiceManager implements IntentCommonService {

    private final IntentActionFactory intentActionFactory;

    public IntentCommonServiceManager(final DataBroker dataBroker,
                                      final OFRendererFlowService ofRendererFlowService,
                                      final IntentStateMachineExecutorService stateMachineExecutorService) {
        this.intentActionFactory = new IntentActionFactory(new CommonUtils(dataBroker), ofRendererFlowService, stateMachineExecutorService);
    }

    @Override
    public void resolveAndApply(Intent intent) {
        final OFRendererService ofRendererService = intentActionFactory.buildBasicRendererService();
        ofRendererService.applyIntent(intent);
    }

    @Override
    public void resolveAndApply(IntentLimiter intentLimiter) {
        //TODO: Make it receive an Object, verify the Instance and get the right renderer service
        final IntentLifeCycleService lifeCycleService = intentActionFactory.buildIntentLimiterService();
        lifeCycleService.startTransaction(intentLimiter.getId().getValue(), EventType.INTENT_CREATED);
    }

    @Override
    public void resolveAndApply(NodeId nodeId) {
        //TODO: Implement the NodeUp event
    }

    @Override
    public void resolveAndRemove(IntentLimiter intentLimiter) {
        final IntentLifeCycleService lifeCycleService = intentActionFactory.buildIntentLimiterService();
        lifeCycleService.startTransaction(intentLimiter.getId().getValue(), EventType.INTENT_REMOVED);
    }

    @Override
    public void resolveAndRemove(Intent intent) {
        final OFRendererService ofRendererService = intentActionFactory.buildBasicRendererService();
        ofRendererService.removeIntent(intent);
    }

    @Override
    public void createARPFlow(NodeId nodeId) {
        final OFRendererService ofRendererService = intentActionFactory.buildBasicRendererService();
        ofRendererService.evaluateArpFlows(nodeId);
    }

    @Override
    public void createLLDPFlow(NodeId nodeId) {
        final OFRendererService ofRendererService = intentActionFactory.buildBasicRendererService();
        ofRendererService.evaluateLLDPFlow(nodeId);
    }

    @Override
    public void init() {
        //TODO: Apply all configuration at the startup
    }

    @Override
    public void stop() {
        //TODO: Make a cleanup at the shutdown
    }
}
