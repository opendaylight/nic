/*
 * Copyright (c) 2016 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.common.model;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;

public class FlowDataL3 extends FlowData {

    public FlowDataL3(
            final IpAddress srcIpAddress,
            final IpAddress dstIpAddress,
            final FlowAction flowAction) {
        super(srcIpAddress, dstIpAddress, flowAction);
    }

    public IpAddress getSrcIpAddress() {
        return super.srcIpAddress;
    }

    public IpAddress getDstIpAddress() {
        return super.dstIpAddress;
    }
}
