/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.action.remove;

import org.opendaylight.nic.common.transaction.exception.RendererServiceException;
import org.opendaylight.nic.common.transaction.service.AbstractActionService;
import org.opendaylight.nic.common.transaction.service.lifecycle.IntentActionListener;
import org.opendaylight.nic.common.transaction.service.lifecycle.IntentActionRegister;
import org.opendaylight.nic.common.transaction.service.lifecycle.IntentLifeCycleListener;
import org.opendaylight.nic.common.transaction.service.lifecycle.IntentLifeCycleService;
import org.opendaylight.nic.common.transaction.service.renderer.RendererService;

/**
 * Created by yrineu on 10/04/17.
 */
public class IntentRemoveAction extends AbstractActionService implements IntentActionRegister, IntentRemoveService {

    public IntentRemoveAction(final IntentLifeCycleService lifeCycleService,
                              final RendererService rendererService) {
        super(lifeCycleService, rendererService);
    }

    private void intentRemoved(final String id) {
        registerLifeCycle(new IntentLifeCycleListener() {
            @Override
            public void transactionStarted() {
                //DO_NOTHING
            }

            @Override
            public void proceedExecution() {
                try {
                    rendererService.evaluateRollBack(id);
                    rendererService.stopSchedule(id);
                    unregisterLifeCycle(this);
                    notifyToProceed(id);
                } catch (RendererServiceException e) {
                    notifyFail(id);
                }
            }

            @Override
            public void stopExecution() {
                unregisterLifeCycle(this);
                notifyFail(id);
            }
        });
    }

    @Override
    public void register(String id, IntentActionListener listener) {
        registerForAction(listener);
    }

    @Override
    public void unregister(IntentActionListener listener) {
        unregisterForAction(listener);
    }

    @Override
    public void start(String id) {
        intentRemoved(id);
    }

    @Override
    public void clearActionListerList() {
        super.removeAllActionListeners();
    }
}
