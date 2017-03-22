/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.renderer;

import org.opendaylight.nic.common.transaction.exception.DelayConfigsCreationException;
import org.opendaylight.nic.common.transaction.exception.RemoveDataflowException;
import org.opendaylight.nic.common.transaction.exception.RemoveDelayconfigException;
import org.opendaylight.nic.common.transaction.exception.RendererServiceException;
import org.opendaylight.nic.common.transaction.utils.CommonUtils;
import org.opendaylight.nic.of.renderer.api.OFRendererFlowService;
import org.opendaylight.nic.of.renderer.exception.MeterCreationExeption;
import org.opendaylight.nic.utils.FlowAction;
import org.opendaylight.nic.utils.exceptions.IntentInvalidException;
import org.opendaylight.nic.utils.exceptions.PushDataflowException;
import org.opendaylight.schedule.ScheduleService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.Dataflow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.DataflowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.delay.config.rev170327.delay.configs.DelayConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.delay.config.rev170327.delay.configs.DelayConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/**
 * Created by yrineu on 10/04/17.
 */
public class OFRendererServiceImpl implements OFRendererService {
    private static final Logger LOG = LoggerFactory.getLogger(OFRendererServiceImpl.class);

    private final CommonUtils commonUtils;
    private final OFRendererFlowService ofRendererFlowService;
    private final ScheduleService scheduleService;

    protected OFRendererServiceImpl(final CommonUtils commonUtils,
                                    final OFRendererFlowService ofRendererFlowService,
                                    final ScheduleService scheduleService) {
        this.commonUtils = commonUtils;
        this.ofRendererFlowService = ofRendererFlowService;
        this.scheduleService = scheduleService;
    }

    @Override
    public synchronized void evaluateAction(String id) throws RendererServiceException {
        try {
            final IntentLimiter intentLimiter = commonUtils.retrieveIntentLimiter(id);
            final Long bandwidtLimit = intentLimiter.getBandwidthLimit();
            final MeterId meterId = createMeter(id, bandwidtLimit);
            final Dataflow dataflow = commonUtils.createFlowData(intentLimiter, meterId);

            sendFlows(dataflow);
            saveDataflow(dataflow);
            createDelayConfigs(intentLimiter, dataflow);
        } catch (ExecutionException | IntentInvalidException e) {
            LOG.error(e.getMessage());
            throw new RendererServiceException(e.getMessage());
        }
    }

    private MeterId createMeter(final String id, final Long bandwidtLimit) throws RendererServiceException {
        try {
            return ofRendererFlowService.createMeter(id, bandwidtLimit);
        } catch (MeterCreationExeption e) {
            LOG.error(e.getMessage());
            throw new RendererServiceException(e.getMessage());
        }
    }

    private Dataflow sendFlows(final Dataflow dataflow) throws RendererServiceException {
        try {
            return ofRendererFlowService.pushDataFlow(dataflow);
        } catch (PushDataflowException e) {
            removeMeter(dataflow.getId().getValue(), dataflow.getMeterId().longValue());
            LOG.error(e.getMessage());
            throw new RendererServiceException(e.getMessage());
        }
    }

    private void saveDataflow(final Dataflow dataflow) throws RendererServiceException {
        try {
            commonUtils.saveDataflow(dataflow);
        } catch (PushDataflowException e) {
            LOG.error(e.getMessage());
            throw new RendererServiceException(e.getMessage());
        }
    }

    private void createDelayConfigs(final IntentLimiter intentLimiter,
                                    final Dataflow dataflow) throws DelayConfigsCreationException {
        final DelayConfig delayConfig = configureDelay(intentLimiter);
        scheduleService.scheduleRefresh(dataflow, delayConfig);
        commonUtils.saveDelayConfig(delayConfig);
    }

    private void removeMeter(final String id, final Long meterId) throws RendererServiceException {
        try {
            ofRendererFlowService.removeMeter(meterId, id);
        } catch (PushDataflowException e) {
            throw new RendererServiceException(e.getMessage());
        }
    }

    @Override
    public synchronized void evaluateRollBack(String id) throws RendererServiceException {
        try {
            final Dataflow dataflow = commonUtils.retrieveDataflow(id);
            if (dataflow != null) {

                final DataflowBuilder dataflowBuilder = new DataflowBuilder(dataflow);
                dataflowBuilder.setRendererAction(Dataflow.RendererAction.REMOVE);
                ofRendererFlowService.pushDataFlow(dataflowBuilder.build());
                commonUtils.removeDelayConfig(id);
                commonUtils.removeDataFlow(id);
            } else {
                LOG.debug("\nDataflow not found or never created.");
            }
        } catch (ExecutionException | RemoveDataflowException | RemoveDelayconfigException e) {
            LOG.error(e.getMessage());
            throw new RendererServiceException(e.getMessage());
        }
    }

    @Override
    public void stopSchedule(String id) {
        scheduleService.stop(id);
    }

    @Override
    public void execute(Dataflow dataflow) {
        try {
            ofRendererFlowService.pushDataFlow(dataflow);
        } catch (PushDataflowException e) {
            LOG.error(e.getMessage());
        }
    }

    private DelayConfig configureDelay(final IntentLimiter intentLimiter) throws DelayConfigsCreationException {
        final DelayConfigBuilder configBuilder = new DelayConfigBuilder();
        try {
            final long delay = intentLimiter.getInterval().longValue();
            configBuilder.setId(intentLimiter.getId());
            configBuilder.setDelay(delay);
            configBuilder.setTimeUnit(intentLimiter.getIntervalType().getName());
        } catch (Exception e) {
            throw new DelayConfigsCreationException(e.getMessage());
        }
        return configBuilder.build();
    }

    @Override
    public void evaluateLLDPFlow(final NodeId nodeId) {
        ofRendererFlowService.pushLLDPFlow(nodeId, FlowAction.ADD_FLOW);
    }

    @Override
    public void evaluateArpFlows(final NodeId nodeId) {
        ofRendererFlowService.pushARPFlow(nodeId, FlowAction.ADD_FLOW);
    }

    @Override
    public void applyIntent(Intent intent) {
        ofRendererFlowService.pushIntentFlow(intent, FlowAction.ADD_FLOW);
    }

    @Override
    public void removeIntent(Intent intent) {
        ofRendererFlowService.pushIntentFlow(intent, FlowAction.REMOVE_FLOW);
    }
}
