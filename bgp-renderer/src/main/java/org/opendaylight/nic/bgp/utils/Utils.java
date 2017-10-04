/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.bgp.utils;

import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.bgp.dataflow.rev170518.BgpDataflows;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Created by yrineu on 20/06/17.
 */
public final class Utils {
    private Utils() {
    }

    public static final InstanceIdentifier<BgpDataflows> BGP_DATAFLOW_IDENTIFIER = InstanceIdentifier
            .builder(BgpDataflows.class).build();
}
