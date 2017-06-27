/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.common.transaction.api.IntentCommonProviderService;
import org.opendaylight.nic.common.transaction.api.IntentCommonService;
import org.opendaylight.nic.engine.IntentStateMachineExecutorService;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;

public class IntentCommonProviderServiceImpl implements IntentCommonProviderService {

    final DataBroker dataBroker;
    final OFRendererFlowService ofRendererFlowService;
    final IntentStateMachineExecutorService stateMachineService;
    protected BundleContext context;
    private ServiceRegistration<IntentCommonProviderService> commonServiceRegistration;
    private IntentCommonService commonService;


    public IntentCommonProviderServiceImpl(final DataBroker dataBroker,
                                           final OFRendererFlowService ofRendererFlowService,
                                           final IntentStateMachineExecutorService stateMachineService) {
        this.dataBroker = dataBroker;
        this.ofRendererFlowService = ofRendererFlowService;
        this.stateMachineService = stateMachineService;
    }

    @Override
    public void start() {
        context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        commonServiceRegistration = context.registerService(IntentCommonProviderService.class, this, null);
    }

    @Override
    public IntentCommonService retrieveCommonServiceInstance() {
        commonService = new IntentCommonServiceManager(
                dataBroker,
                ofRendererFlowService,
                stateMachineService);
        commonService.init();
        return commonService;
    }

    @Override
    public void close() throws Exception {
        commonService.stop();
        commonServiceRegistration.unregister();
    }
}
