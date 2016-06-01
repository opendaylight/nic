/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import com.google.gson.Gson;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.neutron.NeutronSecurityRule;
import org.opendaylight.nic.of.renderer.model.IntentEndPointType;
import org.opendaylight.nic.of.renderer.model.PortFlow;
import org.opendaylight.nic.of.renderer.utils.IntentFlowUtils;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Log;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.ClassificationConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Set;

public class IntentFlowManager extends AbstractFlowManager {

    private List<String> endPointGroups = null;
    private Action action = null;
    private static final Logger LOG = LoggerFactory.getLogger(IntentFlowManager.class);
    private FlowStatisticsListener flowStatisticsListener;
    private Intent intent;
    private String flowName = "";

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
        IntentFlowUtils.validate(endPointGroups);
        IntentFlowUtils.validate(flowAction);

        final IntentEndPointType endPointType = IntentFlowUtils.extractEndPointType(endPointGroups);
        switch (endPointType) {
            case MAC_ADDRESS_BASED:
                final MacAddress srcMacAddress = IntentFlowUtils.extractSrcMacAddress(endPointGroups);
                final MacAddress dstMacAddress = IntentFlowUtils.extractDstMacAddress(endPointGroups);

                pushFlowsByMacAddress(srcMacAddress, dstMacAddress, nodeId, flowAction);
                break;
            case PORT_BASED:
                pushFlowsByPortNumber(nodeId, flowAction);
                break;
            case UNKNOWN:
                String actionClass = action.getClass().getName();
                LOG.error("Invalid action: {}", actionClass);
                break;

        }
    }

    private void pushFlowsByMacAddress(MacAddress srcMacAddress, MacAddress dstMacAddress,
                                       NodeId nodeId, FlowAction flowAction) {
        final MatchBuilder matchBuilder = MatchUtils.createEthMatch(new MatchBuilder(), srcMacAddress, dstMacAddress);
        final FlowBuilder flowBuilder = createFlowBuilder(matchBuilder);

        // TODO: Extend for other actions
        if (action instanceof Allow) {
            // Set allow action
            Instructions buildedInstructions = createOutputInstructions(OutputPortValues.NORMAL);
            flowBuilder.setInstructions(buildedInstructions);

        } else if (action instanceof Log) {
            // Logs the statistics data and return.
            String flowIdName = readDataTransaction(nodeId, flowBuilder);
            if (flowIdName != null) {
                flowStatisticsListener.registerFlowStatisticsListener(dataBroker, nodeId, flowIdName);
            }
        } else {
            String actionClass = action.getClass().getName();
            LOG.error("Invalid action: {}", actionClass);
        }
            writeDataTransaction(nodeId, flowBuilder, flowAction);
    }

    private void pushFlowsByPortNumber(NodeId nodeId, FlowAction flowAction) {
        for (Constraints cons : intent.getConstraints()) {
            /**
             * Code block for security rules flow matches
             */
            if(cons.getConstraints() instanceof ClassificationConstraint) {
                ClassificationConstraint portConstraint = (ClassificationConstraint) cons.getConstraints();
                pushPortFlows(portConstraint, nodeId, flowAction);
            }
        }
    }

    private void pushPortFlows(ClassificationConstraint portConstraint, NodeId nodeId, FlowAction flowAction) {
        String portObject = "";
        try {
            portObject = portConstraint.getClassificationConstraint().getClassifier();
        } catch (NullPointerException npe) {
            throw new InvalidParameterException(CONSTRAINTS_NOT_FOUND_EXCEPTION + npe.getMessage());
        }

        Gson gson = new Gson();
        final NeutronSecurityRule securityRule = gson.fromJson(portObject, NeutronSecurityRule.class);
        final PortFlow portFlow = IntentFlowUtils.extractPortFlow(securityRule, endPointGroups);
        final Set<MatchBuilder> matchBuilders = portFlow.createPortRangeMatchBuilder();

        for(MatchBuilder matchBuilder : matchBuilders) {
            final FlowBuilder flowBuilder = createFlowBuilder(matchBuilder);
            final Instructions builtInstructions = createOutputInstructions(OutputPortValues.NORMAL);
            flowBuilder.setInstructions(builtInstructions);
            flowName = portFlow.getFlowName(intent.getId().getValue());
            writeDataTransaction(nodeId, flowBuilder, flowAction);
        }
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

    @Deprecated
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
