/*
 * Copyright (c) 2018 Lumina Networks.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.listener;

public interface NetworkEventsService extends AutoCloseable {

    void start();

    void register(TopologyListener topologyListener);

    void unRegister(TopologyListener topologyListener);
}
