/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.action.add;

import org.opendaylight.nic.common.transaction.exception.RendererServiceException;
import org.opendaylight.nic.common.transaction.service.AbstractActionService;
import org.opendaylight.nic.common.transaction.service.lifecycle.IntentActionListener;
import org.opendaylight.nic.common.transaction.service.lifecycle.IntentActionRegister;
import org.opendaylight.nic.common.transaction.service.lifecycle.IntentLifeCycleListener;
import org.opendaylight.nic.common.transaction.service.lifecycle.IntentLifeCycleService;
import org.opendaylight.nic.common.transaction.service.renderer.RendererService;

/**
 * Created by yrineu on 07/04/17.
 */
public class IntentAddAction extends AbstractActionService implements IntentActionRegister, IntentAddService {


    public IntentAddAction(final IntentLifeCycleService lifeCycleService,
                           final RendererService rendererService) {
        super(lifeCycleService, rendererService);
    }

    private void intentCreated(final String id) {
        registerLifeCycle(new IntentLifeCycleListener() {

            @Override
            public void transactionStarted() {
                notifyToProceed(id);
            }

            @Override
            public void proceedExecution() {
                try {
                    rendererService.evaluateAction(id);
                    unregisterLifeCycle(this);
                    notifyToProceed(id);
                } catch (RendererServiceException e) {
                    notifyFail(id);
                }
            }

            @Override
            public void stopExecution() {
                try {
                    rendererService.evaluateRollBack(id);
                    notifyToProceed(id);
                    unregisterLifeCycle(this);
                } catch (RendererServiceException e) {
                    notifyFail(id);
                    unregisterLifeCycle(this);
                }
            }
        });
    }

    @Override
    public void register(String id, IntentActionListener listener) {
        super.registerForAction(listener);
    }

    @Override
    public void unregister(final IntentActionListener listener) {
        super.unregisterForAction(listener);
    }

    @Override
    public void start(String id) {
        intentCreated(id);
    }

    @Override
    public void clearActionListerList() {
        super.removeAllActionListeners();
    }
}
