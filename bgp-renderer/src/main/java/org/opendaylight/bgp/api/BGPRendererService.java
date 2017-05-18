/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.bgp.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.BgpDataflow;

/**
 * Service to manage BGP
 */
public interface BGPRendererService extends AutoCloseable {

    /**
     * Start BGP services
     */
    void start();

    /**
     * Advertise route for a given {@link BgpDataflow}
     */
    void advertiseRoute(BgpDataflow bgpDataflow);
}
