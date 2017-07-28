/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.rest;

import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflows.EvpnDataflow;

import java.util.List;

/**
 * Created by yrineu on 25/07/17.
 */
public interface JuniperRestService {

    void sendConfiguration(List<EvpnDataflow> evpnDataflows);
}
