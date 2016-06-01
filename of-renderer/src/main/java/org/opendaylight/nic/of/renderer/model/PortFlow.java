/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.model;

import org.opendaylight.nic.of.renderer.exception.InvalidIntentParameterException;
import org.opendaylight.nic.of.renderer.impl.OFRendererConstants;
import org.opendaylight.nic.of.renderer.utils.IntentFlowUtils;
import org.opendaylight.nic.of.renderer.utils.MatchUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;

import java.util.*;

/**
 * Created by yrineu on 10/06/16.
 */
public class PortFlow {

    private enum IntentProtocolType {
        ProtocolIcmp, ProtocolUdp, ProtocolTcp
    }

    private enum IntentEthernetType {
        EthertypeV4, EthertypeV6
    }

    private enum IntentDirection {
        DirectionIngress, DirectionEgress
    }

    private interface MatchBuilderCreator {
        Set<MatchBuilder> create();
    }

    private interface MatchBuilderCreatorByPort {
        MatchBuilder create(PortNumber portNumber);
    }

    private Map<IntentProtocolType, MatchBuilderCreator> matchBuilderCreatorMap;

    private static final String MIN_OR_MAX_PORTRANGE_INVALID_MSG = "Port MAX and MIN could not be null.";

    private Integer maxPortNumber;
    private Integer minPortNumber;
    private PortNumber portNumber;
    private IntentProtocolType protocolType;
    private String ethernetType;
    private List<String> endPointGroups;
    private IntentDirection direction;
    private Set<MatchBuilder> portMatchBuilders;

    public PortFlow(Integer maxPortNumber,
                    Integer minPortNumber,
                    String protocolType,
                    String ethernetType,
                    String direction,
                    List<String> endPointGroups) {
        this.maxPortNumber = maxPortNumber;
        this.minPortNumber = minPortNumber;
        this.protocolType = IntentProtocolType.valueOf(protocolType);
        this.ethernetType = ethernetType;
        this.direction = IntentDirection.valueOf(direction);
        this.endPointGroups = endPointGroups;

        matchBuilderCreatorMap = new HashMap<>();
        portMatchBuilders = new HashSet<>();
        populateMap();
    }

    public Set<MatchBuilder> createPortRangeMatchBuilder() {
        MatchBuilderCreator matchBuilderCreator = matchBuilderCreatorMap.get(protocolType);
        return matchBuilderCreator.create();
    }

    private MatchBuilder getIpv6MatchBuilder() {
        MatchBuilder matchBuilder = getIcmpMatchBuilder();
        return IntentFlowUtils.createIpv6PrefixMatch(matchBuilder, endPointGroups);
    }

    private MatchBuilder getIcmpMatchBuilder() {
        return MatchUtils.createICMPv4Match(new MatchBuilder(), minPortNumber.shortValue(), maxPortNumber.shortValue());
    }

    private MatchBuilder getIpv4MatchBuilder() {
        MatchBuilder matchBuilder = getIcmpMatchBuilder();
        return IntentFlowUtils.createIpv4PrefixMatch(matchBuilder, endPointGroups);
    }

    private Set<MatchBuilder> getMatchBuilderByDirection() {
        return extractMatchBuildersByPortNumbers(new MatchBuilderCreatorByPort() {
            @Override
            public MatchBuilder create(PortNumber portNumber) {
                MatchBuilder matchBuilder = new MatchBuilder();
                switch (direction) {
                    case DirectionEgress:
                        matchBuilder = getEgressMatchBuilderByProtocolAndPort(portNumber);
                        break;
                    case DirectionIngress:
                        matchBuilder = getIngressMatchBuilderByProtocolAndPort(portNumber);
                }
                return matchBuilder;
            }
        });
    }

    private MatchBuilder getIngressMatchBuilderByProtocolAndPort(PortNumber portNumber) {
        MatchBuilder matchBuilder = new MatchBuilder();
        switch (protocolType) {
            case ProtocolTcp:
                matchBuilder = MatchUtils.createSetSrcTcpMatch(matchBuilder, portNumber);
                break;
            case ProtocolUdp:
                matchBuilder = MatchUtils.createSetSrcUdpMatch(matchBuilder, portNumber);
                break;
        }
        return matchBuilder;
    }

    private MatchBuilder getEgressMatchBuilderByProtocolAndPort(PortNumber portNumber) {
        MatchBuilder matchBuilder = new MatchBuilder();
        switch (protocolType) {
            case ProtocolTcp:
                matchBuilder = MatchUtils.createSetDstTcpMatch(matchBuilder, portNumber);
                break;
            case ProtocolUdp:
                matchBuilder = MatchUtils.createSetDstUdpMatch(matchBuilder, portNumber);
                break;
        }
        return matchBuilder;
    }

    private Set<MatchBuilder> extractMatchBuildersByPortNumbers(MatchBuilderCreatorByPort builderCreator) {
        final Set<MatchBuilder> matchBuilders = new HashSet<>();
        for(int portNumber = minPortNumber; portNumber <= maxPortNumber; portNumber++) {
            MatchBuilder builder = builderCreator.create(new PortNumber(portNumber));
            matchBuilders.add(builder);
        }
        return matchBuilders;
    }

    public String getFlowName (String intentId) {
        return createFlowName(intentId);
    }

    public void validate() {
        if(minPortNumber == null || maxPortNumber == null) {
            throw new InvalidIntentParameterException(MIN_OR_MAX_PORTRANGE_INVALID_MSG);
        }
    }

    private String createICMPFlowName() {
        return ("icmp" + String.valueOf(minPortNumber) + "_" + String.valueOf(maxPortNumber));
    }

    private String createPortFlowName() {
        return ("port" + String.valueOf(portNumber));
    }

    public String createFlowName(String intentId) {
        StringBuilder sb = new StringBuilder();
        sb.append(OFRendererConstants.INTENT_L2_FLOW_NAME);
        sb.append(endPointGroups.get(OFRendererConstants.SRC_END_POINT_GROUP_INDEX));
        sb.append(endPointGroups.get(OFRendererConstants.DST_END_POINT_GROUP_INDEX));
        sb.append(intentId);

        String portName = sb.toString();
        portName += IntentProtocolType.ProtocolIcmp.equals(protocolType) ? createICMPFlowName() : createPortFlowName();
        return portName;
    }

    private void populateMap() {
        matchBuilderCreatorMap.put(IntentProtocolType.ProtocolIcmp, new MatchBuilderCreator() {
            @Override
            public Set<MatchBuilder> create() {
                Set<MatchBuilder> matchBuilders = new HashSet<>();
                final IntentEthernetType ethType = IntentEthernetType.valueOf(ethernetType);
                switch (ethType) {
                    case EthertypeV4:
                        matchBuilders.add(getIpv4MatchBuilder());
                        break;
                    case EthertypeV6:
                        matchBuilders.add(getIpv6MatchBuilder());
                }

                return matchBuilders;
            }
        });

        matchBuilderCreatorMap.put(IntentProtocolType.ProtocolTcp, new MatchBuilderCreator() {
            @Override
            public Set<MatchBuilder> create() {
                return getMatchBuilderByDirection();
            }
        });

        matchBuilderCreatorMap.put(IntentProtocolType.ProtocolUdp, new MatchBuilderCreator() {
            @Override
            public Set<MatchBuilder> create() {
                return getMatchBuilderByDirection();
            }
        });
    }
}