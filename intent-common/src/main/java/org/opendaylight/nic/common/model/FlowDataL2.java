/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.model;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

public class FlowDataL2 extends FlowData {

    public FlowDataL2(final MacAddress srcMacAddress,
                      final MacAddress dstMacAddress,
                      final FlowAction flowAction) {
        super(srcMacAddress, dstMacAddress, flowAction);
    }

    public MacAddress getSrcMacAddress() {
        return super.srcMacAddress;
    }

    public MacAddress getDstMacAddress() {
        return super.dstMacAddress;
    }
}
