/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.of.renderer.model.IntentEndPointType;
import org.opendaylight.nic.of.renderer.utils.IntentFlowUtils;
import org.opendaylight.nic.of.renderer.utils.MatchUtils;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;

public class IntentFlowManager extends AbstractFlowManager {

    private List<String> endPointGroups = null;
    private Action action = null;
    private static final Logger LOG = LoggerFactory.getLogger(IntentFlowManager.class);
    private Intent intent;
    private String flowName = "";
    private static final int RADIX = 10;

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
    }

    @Override
    public void pushFlow(final NodeId nodeId, final FlowAction flowAction) {
        IntentFlowUtils.validate(endPointGroups);
        IntentFlowUtils.validate(flowAction);

        final IntentEndPointType endPointType = IntentFlowUtils.extractEndPointType(endPointGroups);
        switch (endPointType) {
            case MAC_ADDRESS_BASED:
                final MacAddress srcMacAddress = IntentFlowUtils.extractSrcMacAddress(endPointGroups);
                final MacAddress dstMacAddress = IntentFlowUtils.extractDstMacAddress(endPointGroups);

                pushFlowsByMacAddress(srcMacAddress, dstMacAddress, nodeId, flowAction);
                break;
            case UNKNOWN:
                String actionClass = action.getClass().getName();
                LOG.error("Invalid action: {}", actionClass);
                break;

        }
    }

    private void pushFlowsByMacAddress(final MacAddress srcMacAddress, final MacAddress dstMacAddress,
                                       final NodeId nodeId, final FlowAction flowAction) {
        final MatchBuilder matchBuilder = MatchUtils.createEthMatch(new MatchBuilder(), srcMacAddress, dstMacAddress);
        final String flowIdStr = srcMacAddress.getValue() + "_" + dstMacAddress.getValue();
        final FlowBuilder flowBuilder = createFlowBuilder(matchBuilder, new FlowId(flowIdStr));

        // TODO: Extend for other actions
        if (action instanceof Allow) {
            // Set allow action
            Instructions buildedInstructions = createOutputInstructions(OutputPortValues.NORMAL);
            flowBuilder.setInstructions(buildedInstructions);

        } else {
            String actionClass = action.getClass().getName();
            LOG.error("Invalid action: {}", actionClass);
        }
            writeDataTransaction(nodeId, flowBuilder, flowAction);
    }

    private FlowBuilder createFlowBuilder(final MatchBuilder matchBuilder, final FlowId flowId) {
        final Match match = matchBuilder.build();
        final FlowKey key = new FlowKey(flowId);
        final FlowBuilder flowBuilder = new FlowBuilder();
        final BigInteger cookieId = new BigInteger("20", RADIX);

        flowBuilder.setId(flowId);
        flowBuilder.setKey(key);
        flowBuilder.setFlowName(flowName);
        flowBuilder.setCookie(new FlowCookie(cookieId));
        flowBuilder.setCookieMask(new FlowCookie(cookieId));
        flowBuilder.setContainerName(null);
        flowBuilder.setStrict(false);
        flowBuilder.setMatch(match);
        flowBuilder.setFlags(new FlowModFlags(false, false, false, false, false));
        flowBuilder.setBarrier(true);
        flowBuilder.setPriority(OFRendererConstants.DEFAULT_PRIORITY);
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
