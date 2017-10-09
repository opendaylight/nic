/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.utils;

import org.opendaylight.nic.of.renderer.exception.InvalidIntentParameterException;
import org.opendaylight.nic.of.renderer.impl.OFRendererConstants;
import org.opendaylight.nic.of.renderer.model.IntentEndPointType;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by yrineu on 02/06/16.
 */
public class IntentFlowUtils {

    private static final String INVALID_END_POINT_MESSAGE = "EndPoint list null or empty.";
    private static final String INVALID_FLOW_ACTION_MESSAGE = "Invalid Action: ";

    private static final Logger LOG = LoggerFactory.getLogger(IntentFlowUtils.class);

    public static void validate(final List<String> endPointGroups) {
        if(endPointGroups == null || endPointGroups.isEmpty()) {
            LOG.error("EndPoints cannot be null or empty.");
            throw new InvalidIntentParameterException(INVALID_END_POINT_MESSAGE);
        }
    }

    public static void validate(final FlowAction flowAction) {
        if(flowAction == null) {
            LOG.error("FlowAction cannot be null.");
            throw new InvalidIntentParameterException(INVALID_FLOW_ACTION_MESSAGE + flowAction);
        }
    }

    public static void isValidMacAddress(final String macAddress) {
        try {
            new MacAddress(macAddress);
        } catch (IllegalArgumentException iae) {
            throw new InvalidIntentParameterException(iae.getMessage());
        }
    }

    public static MacAddress extractSrcMacAddress(final List<String> endPointGroups) {
        final String srcMacAddress = extractEndPoint(endPointGroups, OFRendererConstants.SRC_END_POINT_GROUP_INDEX);
        isValidMacAddress(srcMacAddress);
        return new MacAddress(srcMacAddress);
    }

    public static MacAddress extractDstMacAddress(final List<String> endPointGroups) {
        final String dstMacAddress = extractEndPoint(endPointGroups, OFRendererConstants.DST_END_POINT_GROUP_INDEX);
        isValidMacAddress(dstMacAddress);
        return new MacAddress(dstMacAddress);
    }

    private static String extractEndPoint(final List<String> endPointGroups, int endPointIndex) {
        final String endPointGroup;
        try {
            endPointGroup = endPointGroups.get(endPointIndex);
        } catch (IndexOutOfBoundsException ie) {
            throw new InvalidIntentParameterException(ie.getMessage());
        }
        return endPointGroup;
    }

    private static void isValidPortNumber(final String portNumber) {
        try {
            new PortNumber(Integer.valueOf(portNumber));
        } catch (IllegalArgumentException e) {
            throw new InvalidIntentParameterException(e.getMessage());
        }
    }

    public static PortNumber extractSrcPortNumber(final List<String> endPointGroups) {
        final String srcPortNumber = extractEndPoint(endPointGroups, OFRendererConstants.SRC_END_POINT_GROUP_INDEX);
        isValidPortNumber(srcPortNumber);
        return new PortNumber(Integer.valueOf(srcPortNumber));
    }

    public static IntentEndPointType extractEndPointType(final List<String> endPointGroups) {
        IntentEndPointType result;
        try {
            extractSrcMacAddress(endPointGroups);
            result = IntentEndPointType.MAC_ADDRESS_BASED;
        } catch (InvalidIntentParameterException ie) {
            try {
                extractSrcPortNumber(endPointGroups);
                result = IntentEndPointType.PORT_BASED;
            } catch (InvalidIntentParameterException e) {
                result = IntentEndPointType.UNKNOWN;
            }
        }
        return result;
    }
}
