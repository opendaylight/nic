/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.renderer;

import com.google.common.collect.Maps;
import org.opendaylight.nic.common.transaction.service.lifecycle.IntentLifeCycleManagement;
import org.opendaylight.nic.common.transaction.service.lifecycle.IntentLifeCycleService;
import org.opendaylight.nic.common.transaction.utils.CommonUtils;
import org.opendaylight.nic.engine.api.IntentStateMachineExecutorService;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.schedule.ScheduleService;
import org.opendaylight.schedule.ScheduleServiceManager;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.isp.prefix.rev170615.intent.isp.prefixes.IntentIspPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yangtools.yang.binding.DataObject;

import java.util.Map;

/**
 * Created by yrineu on 07/04/17.
 */
public class IntentActionFactory {

    private final CommonUtils commonUtils;
    private final OFRendererFlowService ofRendererFlowService;
    private final ScheduleService scheduleService;
    private final IntentStateMachineExecutorService stateMachineExecutorService;

    private Map<Class<? extends DataObject>, IntentLifeCycleService> lifecycleServiceByIntentType;

    public void start() {
        lifecycleServiceByIntentType = Maps.newConcurrentMap();
    }

    public IntentActionFactory(final CommonUtils commonUtils,
                               final OFRendererFlowService ofRendererFlowService,
                               final IntentStateMachineExecutorService stateMachineExecutorService) {
        this.commonUtils = commonUtils;
        this.ofRendererFlowService = ofRendererFlowService;
        this.scheduleService = new ScheduleServiceManager();
        this.stateMachineExecutorService = stateMachineExecutorService;
    }

    public synchronized IntentLifeCycleService buildIntentLimiterService() {
        return startService(IntentLimiter.class, new OFRendererServiceImpl(commonUtils, ofRendererFlowService, scheduleService));
    }

    public synchronized IntentLifeCycleService buildIntentIspPrefixService() {
        return startService(IntentIspPrefix.class, new BGPServiceImpl(commonUtils));
    }

    public synchronized IntentLifeCycleService buildBasicOFRendererService() {
        return startService(Intent.class, new OFRendererServiceImpl(commonUtils, ofRendererFlowService, scheduleService));
    }

    private IntentLifeCycleService startService(final Class<? extends DataObject> intentClass,
                                                final RendererService rendererService) {
        IntentLifeCycleService lifeCycleService = lifecycleServiceByIntentType.get(intentClass);
        if (lifeCycleService == null) {
            scheduleService.setRendererService(rendererService);
            lifeCycleService = new IntentLifeCycleManagement(stateMachineExecutorService, rendererService);
            lifecycleServiceByIntentType.put(intentClass, lifeCycleService);
        }
        lifeCycleService.start();
        System.gc();
        return lifeCycleService;
    }

    public void stop() {
        lifecycleServiceByIntentType.clear();
    }
}
