/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.bgp.service;

import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.BgpDataflows;

/**
 * Service responsible to handle changes on BGP Dataflow tree
 */
public interface BGPDataFlowListenerService extends DataTreeChangeListener<BgpDataflows> {

    /**
     * Start listener service for
     * {@link BgpDataflows}
     */
    void start();

    /**
     * Stop listener services.
     */
    void stop();
}
