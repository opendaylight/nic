/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.utils;

import java.util.List;
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

/**
 * Created by yrineu on 02/06/16.
 */
public final class IntentFlowUtils {

    private static final String INVALID_END_POINT_MESSAGE = "EndPoint list null or empty.";
    private static final String INVALID_FLOW_ACTION_MESSAGE = "Invalid Action: ";
    private static final String INVALID_END_POINT_GROUP_TYPE_MSG = "Invalid End Point group type.";

    private static final Logger LOG = LoggerFactory.getLogger(IntentFlowUtils.class);

    private IntentFlowUtils() {
    }

    public static void validate(final List<String> endPointGroups) {
        if (endPointGroups == null || endPointGroups.isEmpty()) {
            LOG.error("EndPoints cannot be null or empty.");
            throw new InvalidIntentParameterException(INVALID_END_POINT_MESSAGE);
        }
    }

    public static void validate(final FlowAction flowAction) {
        if (flowAction == null) {
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

    public static void isValidIpv4Address(final String ipAddress) {
        try {
            new Ipv4Address(ipAddress);
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

    public static Ipv4Address extractSrcIpAddress(final List<String> endPointGroups) {
        final String srcIpv4Address = extractEndPoint(endPointGroups, OFRendererConstants.SRC_END_POINT_GROUP_INDEX);
        isValidIpv4Address(srcIpv4Address);
        return new Ipv4Address(srcIpv4Address);
    }

    public static Ipv4Address extractDstIpAddress(final List<String> endPointGroups) {
        final String dstIpv4Address = extractEndPoint(endPointGroups, OFRendererConstants.DST_END_POINT_GROUP_INDEX);
        isValidIpv4Address(dstIpv4Address);
        return new Ipv4Address(dstIpv4Address);
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

    public static PortNumber extractDstPortNumber(final List<String> endPointGroups) {
        final String dstPortNumber = extractEndPoint(endPointGroups, OFRendererConstants.DST_END_POINT_GROUP_INDEX);
        isValidPortNumber(dstPortNumber);
        return new PortNumber(Integer.valueOf(dstPortNumber));
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

    public static MatchBuilder createIpv4PrefixMatch(MatchBuilder matchBuilder, final List<String> endPointGroups) {
        String endPointSrc = endPointGroups.get(OFRendererConstants.SRC_END_POINT_GROUP_INDEX);
        String endPointDst = endPointGroups.get(OFRendererConstants.DST_END_POINT_GROUP_INDEX);
        Ipv4Prefix srcIpPrefix = null;
        Ipv4Prefix dstIpPrefix = null;

        try {
            if (!endPointSrc.equalsIgnoreCase(OFRendererConstants.ANY_MATCH)) {
                srcIpPrefix = new Ipv4Prefix(endPointSrc);
            }
            if (!endPointDst.equalsIgnoreCase(OFRendererConstants.ANY_MATCH)) {
                dstIpPrefix = new Ipv4Prefix(endPointDst);
            }
            matchBuilder = MatchUtils.createIPv4PrefixMatch(srcIpPrefix, dstIpPrefix, matchBuilder);
        } catch (IllegalArgumentException e) {
            LOG.error("Can only accept valid IP prefix addresses as subjects", e);
            return null;
        }
        return matchBuilder;
    }

    public static MatchBuilder createIpv6PrefixMatch(MatchBuilder matchBuilder, final List<String> endPointGroups) {
        String endPointSrc = endPointGroups.get(OFRendererConstants.SRC_END_POINT_GROUP_INDEX);
        String endPointDst = endPointGroups.get(OFRendererConstants.DST_END_POINT_GROUP_INDEX);
        Ipv6Prefix srcIpPrefix = null;
        Ipv6Prefix dstIpPrefix = null;

        try {
            if (!endPointSrc.equalsIgnoreCase(OFRendererConstants.ANY_MATCH)) {
                srcIpPrefix = new Ipv6Prefix(endPointSrc);
            }
            if (!endPointDst.equalsIgnoreCase(OFRendererConstants.ANY_MATCH)) {
                dstIpPrefix = new Ipv6Prefix(endPointDst);
            }
            matchBuilder = MatchUtils.createIPv6PrefixMatch(srcIpPrefix, dstIpPrefix, matchBuilder);
        } catch (IllegalArgumentException e) {
            LOG.error("Can only accept valid IP prefix addresses as subjects", e);
            return null;
        }
        return matchBuilder;
    }
}
