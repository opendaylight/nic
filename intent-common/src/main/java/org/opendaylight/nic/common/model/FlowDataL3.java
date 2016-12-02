/*
 * Copyright (c) 2016 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.common.model;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;

public class FlowDataL3 extends FlowData {

    public FlowDataL3(
            final IpPrefix srcIpPrefix,
            final IpPrefix dstIpPrefix,
            final PortNumber srcPortNumber,
            final PortNumber dstPortNumber,
            final Dscp dscp,
            final FlowAction flowAction) {
        super(srcIpPrefix, dstIpPrefix, null, null, srcPortNumber, dstPortNumber, dscp, flowAction);
    }

    public IpPrefix getSrcIpPrefix() {
        return super.srcIpPrefix;
    }

    public IpPrefix getDstIpPrefix() {
        return super.dstIpPrefix;
    }
}
