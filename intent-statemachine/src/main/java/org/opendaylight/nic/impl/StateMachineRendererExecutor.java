/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.impl;

import org.opendaylight.nic.engine.service.StateMachineRendererListener;
import org.opendaylight.nic.engine.service.StateMachineRendererService;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by yrineu on 28/12/15.
 */
public class StateMachineRendererExecutor implements StateMachineRendererService{

    private interface RendererExecutor {
        Future execute();
    }

    private static String errorMessage;
    private static String rendererCanceledMessage;
    private StateMachineRendererListener listener;

    public StateMachineRendererExecutor(StateMachineRendererListener listener) {
        this.listener = listener;
    }

    private StateMachineRendererExecutor() {
    }

    @Override
    public void deploy() {
        this.errorMessage = "Impossible to deploy.";
        this.rendererCanceledMessage = "Deploy was cancelled.";
        handle(new RendererExecutor() {
            @Override
            public Future execute() {
                //TODO: Tell OFRenderer
                return getFutureMock();
            }
        });
    }

    @Override
    public void undeploy() {
        this.errorMessage = "Impossible to execute undeploy.";
        this.rendererCanceledMessage = "Undeploy was cancelled.";
        handle(new RendererExecutor() {
            @Override
            public Future execute() {
                //TODO: Tell OFRenderer
                return getFutureMock();
            }
        });
    }

    private void handle(RendererExecutor executor) {
        Future future = executor.execute();
        if (future.isDone()) {
            listener.onSuccess();
        } else if (future.isCancelled()) {
            listener.onError(rendererCanceledMessage);
        } else {
            listener.onError(errorMessage);
        }
    }

    //WIP: Just for tests propose.
    private Future getFutureMock() {
        return new Future() {
            @Override
            public boolean cancel(boolean b) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public Object get() throws InterruptedException, ExecutionException {
                return null;
            }

            @Override
            public Object get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
                return null;
            }
        };
    }
}
