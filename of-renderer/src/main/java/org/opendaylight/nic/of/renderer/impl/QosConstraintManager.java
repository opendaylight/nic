/*
 * Copyright (c) 2016 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Optional;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.of.renderer.utils.MatchUtils;
import org.opendaylight.nic.pipeline_manager.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Allow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Block;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Log;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.Constraints;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.constraints.constraints.QosConstraint;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.qos.config.Qos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.qos.config.qos.DscpType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.Intents;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QosConstraintManager extends AbstractFlowManager {

    private static final Logger LOG = LoggerFactory.getLogger(QosConstraintManager.class);
    /**
     * The list of EndPointGroups.
     */
    private List<String> endPointGroups = null;

    /**
     * The instance for Action.
     */
    private Action action = null;

    /**
     * The instance for Constraints.
     */
    private Constraints constraint = null;

    /**
     * The Constraint profile name.
     */
    private String qosName = null;

    /**
     * The instance for FlowStatisticsListener.
     */
    private FlowStatisticsListener flowStatisticsListener;

    /**
     * Set the EndPointGroups.
     */
    public void setEndPointGroups(List<String> endPointGroups) {
        this.endPointGroups = endPointGroups;
    }

    /**
     * Set the Action.
     */
    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * Set the Constraints.
     */
    public void setConstraint(Constraints constraint) {
        this.constraint = constraint;
    }

    /**
     * Set the Constraint profile name.
     */
    public void setQosName(String qosName) {
        this.qosName = qosName;
    }

    public static final InstanceIdentifier<Intents> INTENTS_IID = InstanceIdentifier.builder(Intents.class).build();

    /**
     * Construct a new instance that matches the given DataBroker and PipelineManager.
     *
     * @param dataBroker DataBroker.
     * @param pipelineManager PipelineManager.
     */
    QosConstraintManager(DataBroker dataBroker, PipelineManager pipelineManager) {
        super(dataBroker, pipelineManager);
        flowStatisticsListener = new FlowStatisticsListener(dataBroker);
    }

    /**
     * Set the flow for action and constraints.
     */
    @Override
    void pushFlow(NodeId nodeId, FlowAction flowAction) {
        if (endPointGroups == null || action == null) {
            LOG.error("Endpoints and action cannot be null");
            return;
        }

        // Creating match object
        MatchBuilder matchBuilder = new MatchBuilder();
        // Creating Flow object
        FlowBuilder flowBuilder = new FlowBuilder();

        // Create L2 match
        createEthMatch(endPointGroups, matchBuilder);
        // Create Flow
        flowBuilder = createFlowBuilder(matchBuilder);

        if (action instanceof Allow && constraint instanceof QosConstraint) {
            List<Intent> intentList = listIntents();
            for (Intent intentValue : intentList) {
                if (intentValue.getQosConfig() != null) {
                    Qos qosContainerDscp = (Qos) intentValue.getQosConfig().get(0).getQos();
                    Qos qosContainerName = (Qos) intentValue.getQosConfig().get(1).getQos();
                    if (((DscpType) qosContainerName).getDscpType().getName() != null) {
                        String profileNames = ((DscpType) qosContainerName).getDscpType().getName();
                        Dscp dscpValues = ((DscpType) qosContainerDscp).getDscpType().getDscp();
                        if (profileNames.equalsIgnoreCase(qosName)) {
                            Instructions buildedInstructions = createQoSInstructions(dscpValues, OutputPortValues.NORMAL);
                            flowBuilder.setInstructions(buildedInstructions);
                            writeDataTransaction(nodeId, flowBuilder, flowAction);
                            return;
                        }
                    }
                }
            }
            LOG.error("Please fill the QoS Configuration profile.");
        } else {
            String actionClass = action.getClass().getName();
            LOG.error("Invalid action: {}", actionClass);
            return;
        }
    }

    private FlowBuilder createFlowBuilder(MatchBuilder matchBuilder) {
        final Match match = matchBuilder.build();
        // Flow named for convenience and uniqueness
        String flowName = createFlowName();
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

    private void createEthMatch(List<String> endPointGroups, MatchBuilder matchBuilder) {
        String endPointSrc = endPointGroups.get(OFRendererConstants.SRC_END_POINT_GROUP_INDEX);
        String endPointDst = endPointGroups.get(OFRendererConstants.DST_END_POINT_GROUP_INDEX);
        MacAddress srcMac = null;
        MacAddress dstMac = null;

        LOG.info("Creating block intent for endpoints: source{} destination {}", endPointSrc, endPointDst);
        try {
            if (!endPointSrc.equalsIgnoreCase(OFRendererConstants.ANY_MATCH)) {
                srcMac = new MacAddress(endPointSrc);
            }
            if (!endPointDst.equalsIgnoreCase(OFRendererConstants.ANY_MATCH)) {
                dstMac = new MacAddress(endPointDst);
            }
            MatchUtils.createEthMatch(matchBuilder, srcMac, dstMac);
        } catch (IllegalArgumentException e) {
            LOG.error("Can only accept valid MAC addresses as subjects", e);
        }
    }

    @Override
    protected String createFlowName() {
        StringBuilder sb = new StringBuilder();
        sb.append(OFRendererConstants.INTENT_L2_FLOW_NAME);
        sb.append(endPointGroups.get(OFRendererConstants.SRC_END_POINT_GROUP_INDEX));
        sb.append(endPointGroups.get(OFRendererConstants.DST_END_POINT_GROUP_INDEX));
        return sb.toString();
    }

    /**
     * Gets the list of intents from the Data store.
     * @return list of intents.
     */
    private List<Intent> listIntents() {
        List<Intent> listOfIntents = null;
        try {
            ReadOnlyTransaction tx = dataBroker.newReadOnlyTransaction();
            Optional<Intents> intents = tx.read((true) ? LogicalDatastoreType.CONFIGURATION
                    : LogicalDatastoreType.OPERATIONAL, INTENTS_IID).checkedGet();
            if(intents.isPresent()) {
                listOfIntents = intents.get().getIntent();
            }
            else {
                LOG.info("Intent tree was empty!");
            }
        } catch (Exception e) {
            LOG.error("ListIntents: failed: {}", e.getMessage(), e);
        }
        return listOfIntents;
    }
}
