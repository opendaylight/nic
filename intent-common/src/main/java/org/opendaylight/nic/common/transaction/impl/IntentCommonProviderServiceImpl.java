/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.common.transaction.TransactionResult;
import org.opendaylight.nic.common.transaction.api.*;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.utils.MdsalUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class IntentCommonProviderServiceImpl implements IntentCommonProviderService {

    final DataBroker dataBroker;
    final OFRendererFlowService ofRendererFlowService;
    protected BundleContext context;

    public IntentCommonProviderServiceImpl(final DataBroker dataBroker,
                                           final OFRendererFlowService ofRendererFlowService) {
        this.dataBroker = dataBroker;
        this.ofRendererFlowService = ofRendererFlowService;
    }

    @Override
    public void start() {
        context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        context.registerService(IntentCommonProviderService.class, this, null);
    }

    @Override
    public IntentCommonService retrieveCommonServiceInstance() {
        return new IntentCommonServiceImpl(dataBroker, ofRendererFlowService);
    }

    @Override
    public void close() throws Exception {
    }
}
