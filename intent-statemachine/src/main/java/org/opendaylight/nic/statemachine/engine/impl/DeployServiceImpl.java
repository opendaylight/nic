/*
 * Copyright (c) 2015 Hewlett-Packard Enterprise.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.statemachine.engine.impl;

import org.opendaylight.nic.statemachine.engine.StateMachineEngineService;
import org.opendaylight.nic.statemachine.engine.service.DeployService;
import org.opendaylight.nic.statemachine.engine.service.StateMachineRendererService;
import org.opendaylight.nic.statemachine.impl.StateMachineRendererExecutor;
import org.opendaylight.nic.transaction.api.EventType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.nic.transaction.api.IntentTransactionNotifier;

import java.util.HashMap;
import java.util.Map;

public class DeployServiceImpl implements DeployService {

    private interface DeployExecutor {
        void execute ();
    }

    private IntentTransactionNotifier transactionNotifier = null;

    private static final Logger LOG = LoggerFactory.getLogger(DeployServiceImpl.class);
    private Map<Intent.State, DeployExecutor> executorMap;
    private static StateMachineEngineService engineService;
    private static DeployService deployService;
    private static StateMachineRendererService rendererService;

    private DeployServiceImpl(StateMachineEngineService engineService) {
        executorMap = new HashMap<>();
        populateExecutorMap();
        this.engineService = engineService;
        this.rendererService = new StateMachineRendererExecutor(this);

        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<?> serviceReference = bundleContext
                .getServiceReference(IntentTransactionNotifier.class);
        this.transactionNotifier = (IntentTransactionNotifier) bundleContext.getService(serviceReference);
    }

    private void populateExecutorMap() {
        executorMap.put(Intent.State.DEPLOYING, new DeployExecutor() {
            @Override
            public void execute() {
                final Uuid intentId = engineService.getIntentId();
                transactionNotifier.notifyExecutors(intentId);
                engineService.updateTransaction(Intent.State.DEPLOYING);
            }
        });

        executorMap.put(Intent.State.DEPLOYED, new DeployExecutor() {
            @Override
            public void execute() {
                engineService.updateTransaction(Intent.State.DEPLOYED);
            }
        });

        executorMap.put(Intent.State.UNDEPLOYED, new DeployExecutor() {
            @Override
            public void execute() {
                rendererService.undeploy();
                cancel();
            }
        });

        executorMap.put(Intent.State.DEPLOYFAILED, new DeployExecutor() {
            @Override
            public void execute() {
                engineService.changeState(Intent.State.DEPLOYFAILED);
            }
        });
    }

    public static DeployService getInstance(StateMachineEngineService engineService) {
        if(deployService == null) {
            deployService = new DeployServiceImpl(engineService);
        }
        return deployService;
    }

    @Override
    public void execute(final EventType eventType) {
        DeployExecutor deployExecutor = executorMap.get(getNextState(eventType));
        if(deployExecutor != null) {
            deployExecutor.execute();
        } else {
            engineService.changeState(getNextState(eventType));
        }
    }

    @Override
    public void onSuccess() {
        engineService.changeState(Intent.State.DEPLOYED);
    }

    @Override
    public void onError(String message) {
        LOG.error(message);
        engineService.changeState(Intent.State.DEPLOYFAILED);
    }

    @Override
    public void cancel() {
        engineService.changeState(Intent.State.UNDEPLOYED);
    }

    public Intent.State getNextState(EventType eventType) {
        Intent.State result;
        switch (eventType) {
            case NODE_ADDED:
            case INTENT_ADDED:
                result = Intent.State.DEPLOYING;
                break;
            case NODE_REMOVED:
                result = Intent.State.UNDEPLOYED;
                break;
            case INTENT_DEPLOY_SUCCESS:
                result = Intent.State.DEPLOYED;
                break;
            case INTENT_DEPLOY_FAILURE:
                result = Intent.State.DEPLOYFAILED;
                break;
            default:
                result = Intent.State.DEPLOYING;
                break;
        }
        return result;
    }
}
