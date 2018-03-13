/*
 * Copyright (c) 2015 - 2016 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.of.renderer.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.of.renderer.api.Observer;
import org.opendaylight.nic.of.renderer.api.Subject;
import org.opendaylight.nic.of.renderer.exception.DataflowCreationException;
import org.opendaylight.nic.of.renderer.exception.MeterCreationExeption;
import org.opendaylight.nic.of.renderer.listener.NetworkEvents;
import org.opendaylight.nic.of.renderer.listener.NetworkEventsService;
import org.opendaylight.nic.of.renderer.strategy.ActionStrategy;
import org.opendaylight.nic.of.renderer.strategy.DefaultExecutor;
import org.opendaylight.nic.of.renderer.utils.TopologyUtils;
import org.opendaylight.nic.of.renderer.pipeline.PipelineManager;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.IntentUtils;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.nic.utils.exceptions.PushDataflowException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.idmanager.rev160406.IdManagerService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intent.actions.action.Redirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.Dataflow;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by saket on 8/19/15.
 */
public class OFRendererFlowManagerProvider implements OFRendererFlowService, Observer, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(OFRendererFlowManagerProvider.class);
    private IntentFlowManager intentFlowManager;
    private ArpFlowManager arpFlowManager;
    private LldpFlowManager lldpFlowManager;
    private DataBroker dataBroker;
    private final PipelineManager pipelineManager;
    private OFRuleWithMeterManager ofRuleWithMeterManager;
    private Subject topic;
    private IdManagerService idManagerService;
    final NetworkEventsService networkEventsService;


    public OFRendererFlowManagerProvider(final DataBroker dataBroker,
                                         final PipelineManager pipelineManager,
                                         final IdManagerService idManagerService) {
        this.dataBroker = dataBroker;
        this.pipelineManager = pipelineManager;
        this.idManagerService = idManagerService;
        this.networkEventsService = new NetworkEvents(dataBroker);
    }

    @Override
    public void start() {
        LOG.info("OF Renderer Provider Session Initiated");
        networkEventsService.start();
        intentFlowManager = new IntentFlowManager(dataBroker, pipelineManager);
        arpFlowManager = new ArpFlowManager(dataBroker, pipelineManager, networkEventsService);
        lldpFlowManager = new LldpFlowManager(dataBroker, pipelineManager, networkEventsService);
        this.ofRuleWithMeterManager = new OFRuleWithMeterManager(dataBroker, idManagerService);

        arpFlowManager.start();
        lldpFlowManager.start();
    }

    @Override
    public void pushIntentFlow(final Intent intent, final FlowAction flowAction) {
        // TODO: Extend to support other actions
        LOG.info("\nIntent: {}, FlowAction: {}", intent.toString(), flowAction.getValue());

        // Creates QoS configuration and stores profile in the Data Store.
        if (intent.getQosConfig() != null) {
            return;
        }

        //TODO: Change to use Command Pattern
        try {
            ActionStrategy actionStrategy = new DefaultExecutor(intentFlowManager,
                    dataBroker);
            actionStrategy.execute(intent, flowAction);
        } catch (IntentInvalidException ie) {
//            TODO: Implement an action for Exception cases
        }
    }

    protected boolean isRedirect(final Intent intent) {
        Action actionContainer = null;
        try {
            actionContainer = IntentUtils.getAction(intent);
        } catch (IntentInvalidException e) {
            LOG.error(e.getMessage());
            throw new NoSuchElementException(e.getMessage());
        }
        return (Redirect.class.isInstance(actionContainer));
    }

    //FIXME move to a utility class
    @Override
    public void pushARPFlow(final NodeId nodeId, final FlowAction flowAction) {
        arpFlowManager.pushFlow(nodeId, flowAction);
    }

    @Override
    public void close() throws Exception {
        //TODO:Provide implementation for a cleanup
    }

    /**
     * Push a LLDP flow onto an Inventory {@link NodeId} so that
     * OpenDaylight can know how the devices are connected to each others.
     * This function is necessary for OF protocols above 1.0
     *
     * @param nodeId     The Inventory {@link NodeId}
     * @param flowAction The {@link FlowAction} to push
     */
    @Override
    public void pushLLDPFlow(final NodeId nodeId, final FlowAction flowAction) {
        lldpFlowManager.pushFlow(nodeId, flowAction);
    }

    @Override
    public synchronized Dataflow pushDataFlow(Dataflow dataFlow) throws PushDataflowException {
        Dataflow result = null;
        try {
            if (dataFlow.isIsFlowMeter()) {
                switch (dataFlow.getRendererAction()) {
                    case ADD:
                        result = sendDataflow(dataFlow);
                        break;
                    case REMOVE:
                        result = removeDataflow(dataFlow);
                        break;
                }
            }
        } catch (ExecutionException e) {
            throw new PushDataflowException(e);
        }
        return result;
    }

    public Dataflow sendDataflow(final Dataflow dataflow) throws ExecutionException {
        final FlowBuilder flowBuilder = ofRuleWithMeterManager.createFlow(dataflow);
        final Map<Node, List<NodeConnector>> nodeMap = TopologyUtils.getNodes(dataBroker);
        for (Map.Entry<Node, List<NodeConnector>> entry : nodeMap.entrySet()) {
            ofRuleWithMeterManager.sendToMdsal(flowBuilder, entry.getKey().getId());
        }
        return dataflow;
    }

    private Dataflow removeDataflow(final Dataflow dataflow) throws ExecutionException {
        final String dataflowId = dataflow.getId().getValue();
        final FlowBuilder flowBuilder = ofRuleWithMeterManager.createFlow(dataflow);
        final Map<Node, List<NodeConnector>> nodeMap = TopologyUtils.getNodes(dataBroker);
        for (Map.Entry<Node, List<NodeConnector>> entry : nodeMap.entrySet()) {
            ofRuleWithMeterManager.removeFromMdsal(flowBuilder, entry.getKey().getId());
        }
        removeMeter(dataflow.getMeterId().longValue(), dataflowId);
        return dataflow;
    }

    @Override
    public void pushDataFlow(final NodeId nodeId, final Dataflow dataflow) {
        try {
            final FlowBuilder flowBuilder = ofRuleWithMeterManager.createFlow(dataflow);
            ofRuleWithMeterManager.sendToMdsal(flowBuilder, nodeId);
        } catch (DataflowCreationException me) {
            LOG.error(me.getMessage());
        }
    }

    @Override
    public MeterId createMeter(final String id, final long dropRate) throws MeterCreationExeption {
        return ofRuleWithMeterManager.createMeter(id, dropRate);
    }

    @Override
    public void removeMeter(final Long meterId, final String dataflowId) throws PushDataflowException {
        try {
            final Future<RpcResult<Void>> releaseResult = ofRuleWithMeterManager.removeMeter(meterId, dataflowId);
            releaseResult.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new PushDataflowException(e);
        }
    }


    @Override
    public void update() {
        final Intent msg = (Intent) topic.getUpdate(this);
        if (msg != null) {
            pushIntentFlow(msg, FlowAction.ADD_FLOW);
        }
    }

    @Override
    public void setSubject(final Subject sub) {
        this.topic = sub;
    }

    @Override
    public void stop() {
        try {
            close();
            networkEventsService.close();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }
}
