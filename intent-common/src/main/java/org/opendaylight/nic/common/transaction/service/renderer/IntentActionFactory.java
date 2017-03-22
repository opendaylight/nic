/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.renderer;

import org.opendaylight.nic.common.transaction.service.lifecycle.IntentLifeCycleManagement;
import org.opendaylight.nic.common.transaction.service.lifecycle.IntentLifeCycleService;
import org.opendaylight.nic.common.transaction.utils.CommonUtils;
import org.opendaylight.nic.engine.IntentStateMachineExecutorService;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.schedule.ScheduleService;
import org.opendaylight.schedule.ScheduleServiceManager;

/**
 * Created by yrineu on 07/04/17.
 */
public class IntentActionFactory {

    private final CommonUtils commonUtils;
    private final OFRendererFlowService ofRendererFlowService;
    private final ScheduleService scheduleService;
    private final IntentStateMachineExecutorService stateMachineExecutorService;
    private OFRendererService ofRendererService;

    public IntentActionFactory (final CommonUtils commonUtils,
                                final OFRendererFlowService ofRendererFlowService,
                                final IntentStateMachineExecutorService stateMachineExecutorService) {
        this.commonUtils = commonUtils;
        this.ofRendererFlowService = ofRendererFlowService;
        this.scheduleService = new ScheduleServiceManager();
        this.stateMachineExecutorService = stateMachineExecutorService;
    }

    public IntentLifeCycleService buildIntentLimiterService() {
        final RendererService rendererService = new OFRendererServiceImpl(commonUtils, ofRendererFlowService, scheduleService);
        scheduleService.setRendererService(rendererService);
        return new IntentLifeCycleManagement(stateMachineExecutorService, rendererService);
    }

    public OFRendererService buildBasicRendererService() {
        if (ofRendererService == null) {
            ofRendererService = new OFRendererServiceImpl(commonUtils, ofRendererFlowService, scheduleService);
        }
        return ofRendererService;
    }
}
