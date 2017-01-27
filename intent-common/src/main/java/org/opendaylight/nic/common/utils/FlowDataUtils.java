/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.utils;

import org.opendaylight.nic.common.model.*;
import org.opendaylight.nic.common.model.FlowAction;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.subjects.subject.EndPointGroup;

public class FlowDataUtils {

    private static FlowDataL3 buildRendererCommonL3(
            final EndPointGroup src,
            final EndPointGroup dst,
            final FlowAction flowAction) {
        return new FlowDataL3(
                IpAddressBuilder.getDefaultInstance(src.getEndPointGroup().getName()),
                IpAddressBuilder.getDefaultInstance(dst.getEndPointGroup().getName()),
                flowAction);
    }

    private static FlowDataL2 buildRendererCommonL2(
            final EndPointGroup src,
            final EndPointGroup dst,
            final FlowAction flowAction) {
        return new FlowDataL2(
                MacAddress.getDefaultInstance(src.getEndPointGroup().getName()),
                MacAddress.getDefaultInstance(dst.getEndPointGroup().getName()),
                flowAction);
    }

    private static FlowType getFlowType(final EndPointGroup srcEndpoint,
                                       final EndPointGroup dstEndpoint) {
        FlowType result = FlowType.L2;
        boolean isL2;
        boolean isL3;
        final String src = srcEndpoint.getEndPointGroup().getName();
        final String dst = dstEndpoint.getEndPointGroup().getName();

        final boolean srcIsMac = IntentUtils.validateMAC(src);
        final boolean dstIsMac = IntentUtils.validateMAC(dst);

        final boolean srcIsIp = IntentUtils.validateIP(src);
        final boolean dstIsIp = IntentUtils.validateIP(dst);

        isL2 = (srcIsMac && dstIsMac);
        isL3 = (srcIsIp && dstIsIp);

        if (isL2) {
            result = FlowType.L2;
        } else if (isL3) {
            result = FlowType.L3;
        }
        return result;
    }

    public static FlowData generateFlowData(final EndPointGroup srcEndPoint,
                                            final EndPointGroup dstEndPoint,
                                            final FlowAction flowAction) {
        FlowData result = null;
        final FlowType flowType = getFlowType(srcEndPoint, dstEndPoint);
        switch (flowType) {
            case L2:
                result = buildRendererCommonL2(srcEndPoint, dstEndPoint, flowAction);
                break;
            case L3:
                result = buildRendererCommonL3(srcEndPoint, dstEndPoint, flowAction);
                break;
        }
        return result;
    }
}
