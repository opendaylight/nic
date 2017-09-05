/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
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
import org.opendaylight.nic.common.transaction.utils.CommonUtils;
import org.opendaylight.nic.engine.api.IntentStateMachineExecutorService;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.utils.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.evpn.rev170724.intent.evpns.IntentEvpn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.isp.prefix.rev170615.intent.isp.prefixes.IntentIspPrefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yrineu on 10/04/17.
 */
public class IntentCommonServiceManager implements IntentCommonService {
    private static final Logger LOG = LoggerFactory.getLogger(IntentCommonServiceManager.class);
    private final IntentActionFactory intentActionFactory;

    public IntentCommonServiceManager(final DataBroker dataBroker,
                                      final OFRendererFlowService ofRendererFlowService,
                                      final IntentStateMachineExecutorService intentStateMachineExecutorService) {
        this.intentActionFactory = new IntentActionFactory(
                new CommonUtils(dataBroker),
                ofRendererFlowService,
                intentStateMachineExecutorService);
    }

    private interface IntentAction {
        void doExecute(String intentId, IntentLifeCycleService service);
    }

    @Override
    public void start() {
        LOG.info("\nIntent Common session Initiated.");
        intentActionFactory.start();
    }

    @Override
    public void resolveAndApply(Object intent) {
        LOG.debug("\nIntent added: {}", intent.toString());
        executeAction(intent, (intentId, service) -> {
            if (intentId != null && service != null) {
                service.startTransaction(intentId, EventType.INTENT_CREATED);
            }
        });
    }

    @Override
    public void resolveAndRemove(Object intent) {
        LOG.debug("\nIntent removed: {}", intent.toString());
        executeAction(intent, (intentId, service) -> {
            if (intentId != null && service != null) {
                service.startTransaction(intentId, EventType.INTENT_REMOVED);
            }
        });
    }

    private void executeAction(final Object intent, final IntentAction action) {
        String intentId = null;
        IntentLifeCycleService lifeCycleService = null;
        if (IntentLimiter.class.isInstance(intent)) {
            final IntentLimiter intentLimiter = (IntentLimiter) intent;
            intentId = intentLimiter.getId().getValue();
            lifeCycleService = intentActionFactory.buildIntentLimiterService();
        }

        if (IntentIspPrefix.class.isInstance(intent)) {
            final IntentIspPrefix intentIspPrefix = (IntentIspPrefix) intent;
            intentId = intentIspPrefix.getId().getValue();
            lifeCycleService = intentActionFactory.buildIntentIspPrefixService();
        }

        if (Intent.class.isInstance(intent)) {
            final Intent intentFirewall = (Intent) intent;
            intentId = intentFirewall.getId().getValue();
            lifeCycleService = intentActionFactory.buildBasicOFRendererService();
        }

        if (IntentEvpn.class.isInstance(intent)) {
            final IntentEvpn intentEvpn = (IntentEvpn) intent;
            intentId = intentEvpn.getIntentEvpnName();
            lifeCycleService = intentActionFactory.buildEvpnService();
        }

        action.doExecute(intentId, lifeCycleService);
    }

    @Override
    public void stop() {
        intentActionFactory.stop();
    }
}
