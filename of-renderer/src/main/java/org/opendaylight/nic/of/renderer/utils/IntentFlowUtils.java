/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.utils;

import org.opendaylight.nic.neutron.NeutronSecurityRule;
import org.opendaylight.nic.of.renderer.exception.InvalidIntentParameterException;
import org.opendaylight.nic.of.renderer.impl.OFRendererConstants;
import org.opendaylight.nic.of.renderer.model.IntentEndPointType;
import org.opendaylight.nic.of.renderer.model.PortFlow;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
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
    private static final String INVALID_END_POINT_GROUP_TYPE_MSG = "Invalid End Point group type.";

    private static final Logger LOG = LoggerFactory.getLogger(IntentFlowUtils.class);

    public static void validate(List<String> endPointGroups) {
        if(endPointGroups == null || endPointGroups.isEmpty()) {
            LOG.error("EndPoints cannot be null or empty.");
            throw new InvalidIntentParameterException(INVALID_END_POINT_MESSAGE);
        }
    }

    public static void validate(FlowAction flowAction) {
        if(flowAction == null) {
            LOG.error("FlowAction cannot be null.");
            throw new InvalidIntentParameterException(INVALID_FLOW_ACTION_MESSAGE + flowAction);
        }
    }

    public static void isValidMacAddress(String macAddress) {
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

    private static String extractEndPoint(List<String> endPointGroups, int endPointIndex) {
        final String endPointGroup;
        try {
            endPointGroup = endPointGroups.get(endPointIndex);
        } catch (IndexOutOfBoundsException ie) {
            throw new InvalidIntentParameterException(ie.getMessage());
        }
        return endPointGroup;
    }

    private static void isValidPortNumber(String portNumber) {
        try {
            new PortNumber(Integer.valueOf(portNumber));
        } catch (IllegalArgumentException e) {
            throw new InvalidIntentParameterException(e.getMessage());
        }
    }

    public static PortNumber extractSrcPortNumber(List<String> endPointGroups) {
        final String srcPortNumber = extractEndPoint(endPointGroups, OFRendererConstants.SRC_END_POINT_GROUP_INDEX);
        isValidPortNumber(srcPortNumber);
        return new PortNumber(Integer.valueOf(srcPortNumber));
    }

    public static PortNumber extractDstPortNumber(List<String> endPointGroups) {
        final String dstPortNumber = extractEndPoint(endPointGroups, OFRendererConstants.DST_END_POINT_GROUP_INDEX);
        isValidPortNumber(dstPortNumber);
        return new PortNumber(Integer.valueOf(dstPortNumber));
    }

    public static IntentEndPointType extractEndPointType(List<String> endPointGroups) {
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

    public static MatchBuilder createIpv4PrefixMatch(MatchBuilder matchBuilder, List<String> endPointGroups) {
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

    public static MatchBuilder createIpv6PrefixMatch(MatchBuilder matchBuilder, List<String> endPointGroups) {
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

    public static PortFlow extractPortFlow(NeutronSecurityRule securityRule, List<String> endPointgroups) {
        Integer portMin = securityRule.getSecurityRulePortMin();
        Integer portMax = securityRule.getSecurityRulePortMax();
        String etherType = securityRule.getSecurityRuleEthertype();
        String protocol = securityRule.getSecurityRuleProtocol();
        String direction = securityRule.getSecurityRuleDirection();

        final PortFlow portFlow = new PortFlow(portMax, portMin, protocol, etherType, direction, endPointgroups);
        portFlow.validate();
        return portFlow;
    }
}
