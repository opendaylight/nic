/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.engine.IntentStateMachineExecutorService;
import org.opendaylight.nic.listeners.api.EventType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.IpAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

//TODO: In the future, this class must use the MD-SAL to execute transactions
public class IntentStateMachineExecutor implements IntentStateMachineExecutorService, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(IntentStateMachineExecutor.class);

    private ServiceRegistration<IntentStateMachineExecutorService> nicStateMachineServiceRegistration;
    private DataBroker dataBroker;

    public IntentStateMachineExecutor(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    @Override
    public void init() {
        LOG.info("Intent State Machine Session Initiated.");
        BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        nicStateMachineServiceRegistration = context.registerService(IntentStateMachineExecutorService.class, this, null);

    }

    @Override
    public void createTransaction(Intent intent, EventType receivedEvent) {
        //TODO: Create transaction on MD-SAL
    }

    @Override
    public void removeTransactions(Uuid intentId, EventType receivedEvent) {
        //TODO: Use the queue on MD-SAL to remove ready transactions
        //tagged with this intentId
    }

    @Override
    public List<Intent> getUndeployedIntents(IpAddress ipAddress) {
        //TODO: Return all Undeployed Intents from the queue that does match with this IPAddress
        return null;
    }

    @Override
    public void close() throws Exception {
        nicStateMachineServiceRegistration.unregister();
    }
}
