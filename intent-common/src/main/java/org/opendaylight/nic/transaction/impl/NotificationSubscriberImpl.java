/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.transaction.impl;

import org.opendaylight.nic.listeners.api.GraphEdgeAdded;
import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.nic.model.RendererCommon;
import org.opendaylight.nic.transaction.api.NotificationSubscriber;
import org.opendaylight.nic.util.CommonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yrineu on 25/10/16.
 */
public class NotificationSubscriberImpl implements NotificationSubscriber {

    private interface ExecutorService {
        void execute(NicNotification event);
    }
    //TODO: Create executor services for other operations
    private Map<Class, ExecutorService> executorServiceMap;

    public NotificationSubscriberImpl() {
        executorServiceMap = new HashMap<>();
        executorServiceMap.put(GraphEdgeAdded.class, createAddEdegeExecutor());
    }
    @Override
    public void handleEvent(NicNotification event) {
        executorServiceMap.get(event.getClass()).execute(event);
    }

    private ExecutorService createAddEdegeExecutor() {
        return new ExecutorService() {
            @Override
            public void execute(NicNotification event) {
                final GraphEdgeAdded edgeAdded = (GraphEdgeAdded) event;
                final RendererCommon rendererCommon = CommonUtils.convertEdge(edgeAdded);
                //TODO: Call the renderer service
            }
        };
    }
}
