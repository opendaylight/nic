/*
 * Copyright (c) 2016 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.utils;

import org.opendaylight.nic.common.model.FlowAction;
import org.opendaylight.nic.common.model.FlowDataL2;
import org.opendaylight.nic.common.model.FlowDataL3;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefixBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;

public class FlowDataUtils {

    public static FlowDataL3 buildRendererCommonL3(
            final String src,
            final String dst,
            final String flowAction) {
        return new FlowDataL3(
                IpPrefixBuilder.getDefaultInstance(src),
                IpPrefixBuilder.getDefaultInstance(dst),
                FlowAction.valueOf(flowAction));
    }

    public static FlowDataL2 buildRendererCommonL2(
            final String src,
            final String dst,
            final String flowAction) {
        return new FlowDataL2(
                MacAddress.getDefaultInstance(src),
                MacAddress.getDefaultInstance(dst),
                FlowAction.valueOf(flowAction));
    }
}
