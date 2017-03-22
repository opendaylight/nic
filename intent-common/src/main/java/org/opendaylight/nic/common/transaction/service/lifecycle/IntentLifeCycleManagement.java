/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.lifecycle;

import org.opendaylight.nic.common.transaction.service.IntentService;
import org.opendaylight.nic.common.transaction.service.action.add.IntentAddAction;
import org.opendaylight.nic.common.transaction.service.action.remove.IntentRemoveAction;
import org.opendaylight.nic.common.transaction.service.renderer.RendererService;
import org.opendaylight.nic.common.transaction.utils.CommonUtils;
import org.opendaylight.nic.engine.IntentStateMachineExecutorService;
import org.opendaylight.nic.utils.EventType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.opendaylight.nic.utils.EventType.*;

/**
 * Created by yrineu on 07/04/17.
 */
public class IntentLifeCycleManagement implements IntentLifeCycleRegister, IntentLifeCycleService {

    private abstract class EventExecutor implements IntentActionListener {

        abstract void doExecute(String id);
    }

    private final Set<IntentLifeCycleListener> lifeCycleListeners;
    private final Map<EventType, EventExecutor> executorByEvent;
    private final IntentStateMachineExecutorService stateMachineExecutorService;
    private final RendererService rendererService;
    protected IntentActionRegister intentAddActionRegister;
    private IntentService intentService;

    public IntentLifeCycleManagement(final IntentStateMachineExecutorService stateMachineExecutorService,
                                     final RendererService rendererService) {
        this.stateMachineExecutorService = stateMachineExecutorService;
        this.lifeCycleListeners = ConcurrentHashMap.newKeySet();
        this.executorByEvent = new HashMap<>();
        this.rendererService = rendererService;
        //TODO: Remove this too logic from this constructor
        populateEventHandler();
    }

    private void populateEventHandler() {
        final EventExecutor INTENT_CREATED_EXECUTOR = new EventExecutor() {

            @Override
            public void doExecute(String id) {
                intentService.clearActionListerList();
                intentService.start(id);
                intentAddActionRegister.register(id, this);
                stateMachineExecutorService.createTransaction(id, INTENT_CREATED);
                CommonUtils.waitUnlock();
                notifyStarted();
            }

            @Override
            public void proceedToNext(String id) {
                executorByEvent.get(INTENT_BEING_ADDED).doExecute(id);
            }

            @Override
            public void proceedToNextFailed(String id) {
                //DO_NOTHING
            }
        };

        final EventExecutor INTENT_BEING_ADDED_EXECUTOR = new EventExecutor() {
            @Override
            void doExecute(String id) {
                intentService.clearActionListerList();
                CommonUtils.waitUnlock();
                intentAddActionRegister.register(id, this);
                stateMachineExecutorService.goToNextTransaction(id, INTENT_BEING_ADDED);
                notifyToProceed();
            }

            @Override
            public void proceedToNext(String id) {
                CommonUtils.waitUnlock();
                lifeCycleListeners.clear();
                stateMachineExecutorService.goToNextTransaction(id, INTENT_ADDED_WITH_SUCCESS);
            }

            @Override
            public void proceedToNextFailed(String id) {
                CommonUtils.waitUnlock();
                stateMachineExecutorService.goToNextTransaction(id, INTENT_ADDED_ERROR);
                boolean canEvaluateAttempt = stateMachineExecutorService.canEvaluateAttempt(id, INTENT_ADD_ATTEMPT);
                if (canEvaluateAttempt) {
                    CommonUtils.waitUnlock();
                    stateMachineExecutorService.goToNextTransaction(id, INTENT_ADD_ATTEMPT);
                    notifyToProceed();
                } else {
                    stateMachineExecutorService.goToNextTransaction(id, INTENT_ADD_RETRY_WITH_MAX_ATTEMPTS);
                    executorByEvent.get(INTENT_DISABLE).doExecute(id);
                }
            }
        };

        final EventExecutor INTENT_REMOVED_EXECUTOR = new EventExecutor() {
            @Override
            void doExecute(String id) {
                intentService.clearActionListerList();
                intentService.start(id);
                intentAddActionRegister.register(id, this);
                stateMachineExecutorService.goToNextTransaction(id, INTENT_BEING_REMOVED);
                notifyToProceed();
            }

            @Override
            public void proceedToNext(String id) {
                CommonUtils.waitUnlock();
                lifeCycleListeners.clear();
                stateMachineExecutorService.goToNextTransaction(id, INTENT_REMOVED_WITH_SUCCESS);
            }

            @Override
            public void proceedToNextFailed(String id) {
                CommonUtils.waitUnlock();
                stateMachineExecutorService.goToNextTransaction(id, INTENT_REMOVE_ERROR);
                boolean canEvaluateAttempt = stateMachineExecutorService.canEvaluateAttempt(id, INTENT_REMOVE_ATTEMPT);
                if (canEvaluateAttempt) {
                    stateMachineExecutorService.goToNextTransaction(id, INTENT_REMOVE_ATTEMPT);
                    notifyToProceed();
                } else {
                    stateMachineExecutorService.goToNextTransaction(id, INTENT_REMOVE_RETRY_WITH_MAX_ATTEMPTS);
                    executorByEvent.get(INTENT_DISABLE).doExecute(id);
                }
            }
        };

        final EventExecutor INTENT_DISABLE_EXECUTOR = new EventExecutor() {
            @Override
            void doExecute(String id) {
                intentService.clearActionListerList();
                CommonUtils.waitUnlock();
                intentAddActionRegister.register(id, this);
                stateMachineExecutorService.goToNextTransaction(id, INTENT_BEING_DISABLED);
                notifyToStopProcess();
            }

            @Override
            public void proceedToNext(String id) {
                CommonUtils.waitUnlock();
                lifeCycleListeners.clear();
                stateMachineExecutorService.goToNextTransaction(id, INTENT_DISABLED);
            }

            @Override
            public void proceedToNextFailed(String id) {
                CommonUtils.waitUnlock();
                lifeCycleListeners.clear();
                stateMachineExecutorService.goToNextTransaction(id, INTENT_DISABLED_WITH_INCONSISTENCIES);
            }
        };
        executorByEvent.put(INTENT_CREATED, INTENT_CREATED_EXECUTOR);
        executorByEvent.put(INTENT_BEING_ADDED, INTENT_BEING_ADDED_EXECUTOR);
        executorByEvent.put(INTENT_REMOVED, INTENT_REMOVED_EXECUTOR);
        executorByEvent.put(INTENT_DISABLE, INTENT_DISABLE_EXECUTOR);
    }

    @Override
    public void startTransaction(final String id,
                                 final EventType eventType) {
        switch (eventType) {
            case INTENT_CREATED:
                this.intentService = new IntentAddAction(this,  rendererService);
                this.intentAddActionRegister = (IntentActionRegister) intentService;
                executorByEvent.get(INTENT_CREATED).doExecute(id);
                break;
            case INTENT_REMOVED:
                this.intentService = new IntentRemoveAction(this, rendererService);
                this.intentAddActionRegister = (IntentActionRegister) intentService;
                executorByEvent.get(INTENT_REMOVED).doExecute(id);
                break;
        }
    }

    @Override
    public void register(IntentLifeCycleListener listener) {
        lifeCycleListeners.add(listener);
    }

    @Override
    public void unregister(IntentLifeCycleListener listener) {
        lifeCycleListeners.remove(listener);
    }

    private void notifyStarted() {
        lifeCycleListeners.forEach(listener -> listener.transactionStarted());
    }

    private void notifyToProceed() {
        lifeCycleListeners.forEach(listener -> listener.proceedExecution());
    }

    private void notifyToStopProcess() {
        lifeCycleListeners.forEach(listener -> listener.stopExecution());
    }
}
