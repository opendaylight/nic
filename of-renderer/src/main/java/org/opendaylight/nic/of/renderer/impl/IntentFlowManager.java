/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.neutron.NeutronSecurityRule;
import org.opendaylight.nic.of.renderer.utils.MatchUtils;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.ClassificationConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Log;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntentFlowManager extends AbstractFlowManager {

    private List<String> endPointGroups = null;
    private Action action = null;
    private static final Logger LOG = LoggerFactory.getLogger(IntentFlowManager.class);
    private FlowStatisticsListener flowStatisticsListener;
    private Intent intent;
    private String flowName = "";

    private static final String ENDPOINTGROUP_NOT_FOUND_EXCEPTION = "EndPoint not found! ";
    private static final String CONSTRAINTS_NOT_FOUND_EXCEPTION = "Constraints not found! ";

    public void setEndPointGroups(List<String> endPointGroups) {
        this.endPointGroups = endPointGroups;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    IntentFlowManager(DataBroker dataBroker, PipelineManager pipelineManager) {
        super(dataBroker, pipelineManager);
        flowStatisticsListener = new FlowStatisticsListener(dataBroker);
    }

    @Override
    public void pushFlow(NodeId nodeId, FlowAction flowAction) {
        if (endPointGroups == null || action == null) {
            LOG.error("Endpoints and action cannot be null");
            return;
        }
        // Creating match object
        MatchBuilder matchBuilder = new MatchBuilder();
        // Flow object
        FlowBuilder flowBuilder;
        final String endPointSrc;
        final String endPointDst;
        try {
            endPointSrc = endPointGroups.get(OFRendererConstants.SRC_END_POINT_GROUP_INDEX);
            endPointDst = endPointGroups.get(OFRendererConstants.DST_END_POINT_GROUP_INDEX);
        } catch (IndexOutOfBoundsException ie) {
            throw new IllegalArgumentException(ENDPOINTGROUP_NOT_FOUND_EXCEPTION + ie.getMessage());
        }
        // Regex for MAC address validation
        Pattern macPattern = Pattern.compile("([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}");
        Matcher macMatcherSrc = macPattern.matcher(endPointSrc);
        Matcher macMatcherDst = macPattern.matcher(endPointDst);
        if (macMatcherSrc.matches() && macMatcherDst.matches()) {
            // Create L2 match
            matchBuilder = createEthMatch(endPointGroups, matchBuilder);
        }
        else {
            for (Constraints cons : intent.getConstraints()) {
                /**
                 * Code block for security rules flow matches
                 */
                if(cons.getConstraints() instanceof ClassificationConstraint) {
                    ClassificationConstraint portConstraint = (ClassificationConstraint) cons.getConstraints();
                    pushPortFlows(portConstraint, nodeId, flowAction);
                }
            }
            return;
        }
        // Create Flow
        // Flow named for convenience and uniqueness
        flowName = createFlowName();
        flowBuilder = createFlowBuilder(matchBuilder);
        // TODO: Extend for other actions
        if (action instanceof Allow) {
            // Set allow action
            Instructions buildedInstructions = createOutputInstructions(OutputPortValues.NORMAL);
            flowBuilder.setInstructions(buildedInstructions);

        } else if (action instanceof Block) {
            // For Block Action the Instructions are not set
            // If block added for readability
        } else if (action instanceof Log) {
            // Logs the statistics data and return.
            String flowIdName = readDataTransaction(nodeId, flowBuilder);
            if (flowIdName != null) {
                flowStatisticsListener.registerFlowStatisticsListener(dataBroker, nodeId, flowIdName);
            }
            return;
        } else {
            String actionClass = action.getClass().getName();
            LOG.error("Invalid action: {}", actionClass);
            return;
        }

        writeDataTransaction(nodeId, flowBuilder, flowAction);
    }

    private void pushPortFlows(ClassificationConstraint portConstraint, NodeId nodeId, FlowAction flowAction) {
        String portObject = "";
        try {
            portObject = portConstraint.getClassificationConstraint().getClassifier();
        } catch (NullPointerException npe) {
            throw new InvalidParameterException(CONSTRAINTS_NOT_FOUND_EXCEPTION + npe.getMessage());
        }
        // Creating match object
        MatchBuilder matchBuilder;
        // Flow object
        FlowBuilder flowBuilder;
        Gson gson = new Gson();
        NeutronSecurityRule securityRule = gson.fromJson(portObject, NeutronSecurityRule.class);
        Integer portMin = securityRule.getSecurityRulePortMin();
        Integer portMax = securityRule.getSecurityRulePortMax();
        String etherType = securityRule.getSecurityRuleEthertype();
        String protocol = securityRule.getSecurityRuleProtocol();
        String direction = securityRule.getSecurityRuleDirection();

        if (portMin != null && portMax != null) {
            if (protocol.compareTo("ProtocolIcmp") == 0) {
                matchBuilder = new MatchBuilder();
                //For ICMP portMin is the type and portMax is the code
                matchBuilder = MatchUtils.createICMPv4Match(matchBuilder, portMin.shortValue(), portMax.shortValue());
                if (etherType.compareTo("EthertypeV4") == 0) {
                    matchBuilder = createIpv4PrefixMatch(matchBuilder);
                }
                else if (etherType.compareTo("EthertypeV6") == 0) {
                    matchBuilder = createIpv6PrefixMatch(matchBuilder);
                }
                // Create Flow
                flowName= createFlowName();
                flowName += "icmp" + String.valueOf(portMin) + "_" + String.valueOf(portMax);
                flowBuilder = createFlowBuilder(matchBuilder);
                //Create Allow action
                Instructions builtInstructions = createOutputInstructions(OutputPortValues.NORMAL);
                flowBuilder.setInstructions(builtInstructions);
                writeDataTransaction(nodeId, flowBuilder, flowAction);
            }
            if (portMin <= portMax) {
                for (int i = portMin; i <= portMax ; i++) {
                    matchBuilder = new MatchBuilder();
                    //TODO: Instead of pushing one flow per port match use port range matchers
                    //TODO: once the following patch is merged to master on openflow plugin
                    //TODO: https://git.opendaylight.org/gerrit/#/c/31388/
                    matchBuilder = createPortMatch(matchBuilder, i, protocol, direction);
                    if (etherType.compareTo("EthertypeV4") == 0) {
                        matchBuilder = createIpv4PrefixMatch(matchBuilder);
                    }
                    else if (etherType.compareTo("EthertypeV6") == 0) {
                        matchBuilder = createIpv6PrefixMatch(matchBuilder);
                    }
                    // Create Flow
                    flowName= createFlowName();
                    flowName += "port" + String.valueOf(i);
                    flowBuilder = createFlowBuilder(matchBuilder);
                    //Create Allow action
                    Instructions builtInstructions = createOutputInstructions(OutputPortValues.NORMAL);
                    flowBuilder.setInstructions(builtInstructions);
                    writeDataTransaction(nodeId, flowBuilder, flowAction);
                }
            }
        }
    }

    private MatchBuilder createPortMatch(MatchBuilder matchBuilder, Integer port,
                                         String protocol, String direction) {
        PortNumber portNumber = new PortNumber(port);
        if (direction.compareTo("DirectionIngress") == 0) {
            if (protocol.compareTo("ProtocolTcp") == 0) {
                matchBuilder = MatchUtils.createSetSrcTcpMatch(matchBuilder, portNumber);
            }
            else if (protocol.compareTo("ProtocolUdp") == 0) {
                matchBuilder = MatchUtils.createSetSrcUdpMatch(matchBuilder, portNumber);
            }
        }
        else if (direction.compareTo("DirectionEgress") == 0) {
            if (protocol.compareTo("ProtocolTcp") == 0) {
                matchBuilder = MatchUtils.createSetDstTcpMatch(matchBuilder, portNumber);
            }
            else if (protocol.compareTo("ProtocolUdp") == 0) {
                matchBuilder = MatchUtils.createSetDstUdpMatch(matchBuilder, portNumber);
            }
        }
        return matchBuilder;
    }

    private MatchBuilder createIpv4PrefixMatch(MatchBuilder matchBuilder) {
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

    private MatchBuilder createIpv6PrefixMatch(MatchBuilder matchBuilder) {
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

    private FlowBuilder createFlowBuilder(MatchBuilder matchBuilder) {
        final Match match = matchBuilder.build();
        final FlowId flowId = new FlowId(flowName);
        final FlowKey key = new FlowKey(flowId);
        final FlowBuilder flowBuilder = new FlowBuilder();

        flowBuilder.setMatch(match);
        flowBuilder.setId(flowId);
        flowBuilder.setKey(key);
        flowBuilder.setBarrier(true);
        flowBuilder.setPriority(OFRendererConstants.DEFAULT_PRIORITY);
        flowBuilder.setFlowName(flowName);
        flowBuilder.setHardTimeout(OFRendererConstants.DEFAULT_HARD_TIMEOUT);
        flowBuilder.setIdleTimeout(OFRendererConstants.DEFAULT_IDLE_TIMEOUT);

        return flowBuilder;
    }

    private MatchBuilder createEthMatch(List<String> endPointGroups, MatchBuilder matchBuilder) {
        String endPointSrc = endPointGroups.get(OFRendererConstants.SRC_END_POINT_GROUP_INDEX);
        String endPointDst = endPointGroups.get(OFRendererConstants.DST_END_POINT_GROUP_INDEX);
        MacAddress srcMac = null;
        MacAddress dstMac = null;

        try {
            if (!endPointSrc.equalsIgnoreCase(OFRendererConstants.ANY_MATCH)) {
                srcMac = new MacAddress(endPointSrc);
            }
            if (!endPointDst.equalsIgnoreCase(OFRendererConstants.ANY_MATCH)) {
                dstMac = new MacAddress(endPointDst);
            }
            matchBuilder = MatchUtils.createEthMatch(matchBuilder, srcMac, dstMac);
        } catch (IllegalArgumentException e) {
            LOG.error("Can only accept valid MAC addresses as subjects", e);
            return null;
        }
        return matchBuilder;
    }

    @Override
    protected String createFlowName() {
        StringBuilder sb = new StringBuilder();
        sb.append(OFRendererConstants.INTENT_L2_FLOW_NAME);
        sb.append(endPointGroups.get(OFRendererConstants.SRC_END_POINT_GROUP_INDEX));
        sb.append(endPointGroups.get(OFRendererConstants.DST_END_POINT_GROUP_INDEX));
        sb.append(intent.getId().getValue());
        return sb.toString();
    }


}
