/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service;

import com.google.common.collect.Sets;
import org.opendaylight.nic.common.transaction.service.lifecycle.IntentActionListener;
import org.opendaylight.nic.common.transaction.service.lifecycle.IntentLifeCycleListener;
import org.opendaylight.nic.common.transaction.service.lifecycle.IntentLifeCycleRegister;
import org.opendaylight.nic.common.transaction.service.lifecycle.IntentLifeCycleService;
import org.opendaylight.nic.common.transaction.service.renderer.RendererService;

import java.util.Set;

/**
 * Created by yrineu on 10/04/17.
 */
public class AbstractActionService {

    private IntentLifeCycleRegister lifeCycleRegister;
    protected IntentLifeCycleService lifeCycleService;
    private Set<IntentActionListener> actionListeners;
    protected RendererService rendererService;

    protected AbstractActionService(final IntentLifeCycleService lifeCycleService,
                           final RendererService rendererService) {
        this.lifeCycleService = lifeCycleService;
        this.lifeCycleRegister = (IntentLifeCycleRegister) lifeCycleService;
        this.actionListeners = Sets.newConcurrentHashSet();
        this.rendererService = rendererService;
    }

    protected void registerForAction(final IntentActionListener listener) {
        actionListeners.add(listener);
    }

    protected void unregisterForAction(final IntentActionListener listener) {
        actionListeners.remove(listener);
    }

    protected void registerLifeCycle(final IntentLifeCycleListener listener) {
        lifeCycleRegister.register(listener);
    }

    protected void unregisterLifeCycle(final IntentLifeCycleListener listener) {
        lifeCycleRegister.unregister(listener);
    }

    protected void notifyToProceed(String id) {
        actionListeners.forEach(listener -> listener.proceedToNext(id));
    }

    protected void notifyFail(String id) {
        actionListeners.forEach(listener -> listener.proceedToNextFailed(id));
    }

    protected void removeAllActionListeners() {
        actionListeners.clear();
    }
}
