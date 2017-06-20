/*
 * Copyright (c) 2017 Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.pubsub.service;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.pubsub.util.Utils;
import org.opendaylight.nic.pubsub.webservice.WebServiceMitigationNotifierOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yrineu on 25/05/17.
 */
public class ActionExecutorServiceImpl implements ActionExecutorService {
    private static final Logger LOG = LoggerFactory.getLogger(ActionExecutorServiceImpl.class);

    private final ExecutorService executorService;
    private final DataBroker dataBroker;

    public ActionExecutorServiceImpl(final DataBroker dataBroker) {
        this.executorService = Executors.newFixedThreadPool(50);
        this.dataBroker = dataBroker;
    }

    @Override
    public void notifyMitigatedAction(final String nodeIp) {
        Utils.getExternalSubscribers(dataBroker).forEach(external ->
                executorService.execute(new WebServiceMitigationNotifierOperation(external.getExternalUrl(), nodeIp)));
    }

    @Override
    public void close() {
        executorService.shutdown();
    }
}
