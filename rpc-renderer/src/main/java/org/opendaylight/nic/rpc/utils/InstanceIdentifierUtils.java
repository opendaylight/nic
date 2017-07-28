/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.rpc.utils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.EvpnDataflows;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by yrineu on 25/07/17.
 */
public class InstanceIdentifierUtils {

    public static final InstanceIdentifier<EvpnDataflows> EVPN_DATAFLOW_IDENTIFIER = InstanceIdentifier
            .builder(EvpnDataflows.class).build();
}
