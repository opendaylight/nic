/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.model;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

/**
 * Created by yrineu on 04/10/16.
 */
public class RendererCommonL3 extends RendererCommon {

    public RendererCommonL3(MacAddress srcMacAddress, MacAddress dstMacAddress) {
        super(srcMacAddress, dstMacAddress);
    }

    public MacAddress getSrcMacAddress() {
        return super.srcMacAddress;
    }

    public MacAddress getDstMacAddress() {
        return super.dstMacAddress;
    }
}
