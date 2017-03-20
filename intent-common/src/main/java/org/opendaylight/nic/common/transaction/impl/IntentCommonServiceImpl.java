/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.common.transaction.api.IntentCommonService;
import org.opendaylight.nic.common.transaction.utils.CommonUtils;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.Dataflow.DataflowMeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.Dataflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.DataflowBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class IntentCommonServiceImpl  implements IntentCommonService {
    private static final Logger LOG = LoggerFactory.getLogger(IntentCommonServiceImpl.class);

    private final CommonUtils commonUtils;
    private OFRendererFlowService ofRendererFlowService;

    public IntentCommonServiceImpl(final DataBroker dataBroker,
                                   final OFRendererFlowService ofRendererFlowService) {
        this.commonUtils = new CommonUtils(dataBroker);
        this.ofRendererFlowService = ofRendererFlowService;
    }

    @Override
    public void resolveAndApply(final Intent intent) {
        //TODO:Change to use this way
        ofRendererFlowService.pushIntentFlow(intent, FlowAction.ADD_FLOW);
    }

    @Override
    public void resolveAndApply(IntentLimiter intentLimiter) {
        try {
            final Dataflow dataflow = createFlowData(intentLimiter);
            final Map<Boolean, Dataflow> result = ofRendererFlowService.pushDataFlow(dataflow);
            result.entrySet().forEach(consumer -> {
                if (consumer.getKey()) {
                    commonUtils.pushDataflow(consumer.getValue());
                }});
        } catch (IntentInvalidException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void resolveAndApply(NodeId nodeId) {
        final List<IntentLimiter> limiters = CommonUtils.retrieveIntentLimiters();
        for (IntentLimiter intentLimiter : limiters) {
            final Dataflow dataflow = commonUtils.retrieveDataflow(intentLimiter.getId().getValue());
            ofRendererFlowService.pushDataFlow(nodeId, dataflow);
        }
    }

    @Override
    public void resolveAndRemove(IntentLimiter intentLimiter) {
        final Dataflow dataflow = commonUtils.retrieveDataflow(intentLimiter.getId().getValue());
        final DataflowBuilder dataflowBuilder = new DataflowBuilder();
        dataflowBuilder.fieldsFrom(dataflow);
        dataflowBuilder.setRendererAction(Dataflow.RendererAction.REMOVE);
        final Map<Boolean, Dataflow> result = ofRendererFlowService.pushDataFlow(dataflowBuilder.build());
        result.entrySet().forEach(consumer -> {
            if (consumer.getKey()) {
                commonUtils.removeDataFlow(consumer.getValue());
            }
        });
    }

    @Override
    public void resolveAndRemove(NodeId nodeId) {

    }

    @Override
    public void resolveAndRemove(Intent intent) {
        ofRendererFlowService.pushIntentFlow(intent, FlowAction.REMOVE_FLOW);
    }

    @Override
    public void resolveAndApply(String intentId) {
        Intent result = null;
        for(Intent intent : commonUtils.retrieveIntents()) {
            if(intent.getId().equals(intentId)) {
                result = intent;
                break;
            }
        }
        if(null != result) {
            resolveAndApply(result);
        }
    }

    @Override
    public void createARPFlow(NodeId nodeId) {
        ofRendererFlowService.pushARPFlow(nodeId, FlowAction.ADD_FLOW);
    }

    @Override
    public void createLLDPFlow(NodeId nodeId) {
        ofRendererFlowService.pushLLDPFlow(nodeId, FlowAction.ADD_FLOW);
    }


    private Dataflow createFlowData(IntentLimiter intent) throws IntentInvalidException {
        final Ipv4Prefix sourceIp = intent.getSourceIp();
        DataflowBuilder dataflowBuilder = new DataflowBuilder();
        dataflowBuilder.setIsFlowMeter(true);
        dataflowBuilder.setId(intent.getId());
        dataflowBuilder.setTimeout(intent.getDuration());
        dataflowBuilder.setDataflowMeterBandType(DataflowMeterBandType.OFMBTDROP);
        dataflowBuilder.setMeterFlags(Dataflow.MeterFlags.METERKBPS);
        dataflowBuilder.setSourceIpAddress(sourceIp);
        dataflowBuilder.setRendererAction(Dataflow.RendererAction.ADD);
        dataflowBuilder.setBandwidthRate(intent.getBandwidthLimit());
        dataflowBuilder.setFlowType(Dataflow.FlowType.L3);
        dataflowBuilder.setMeterId((short)0);
        dataflowBuilder.setStatus(Dataflow.Status.PROCESSING);

        return dataflowBuilder.build();
    }
}
