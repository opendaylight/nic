/*
 * Copyright (c) 2017 Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.pubsub.service;

/**
 * Created by yrineu on 25/05/17.
 */
public interface ActionExecutorService {

    void notifyMitigatedAction(String nodeIp);

    void close();
}
