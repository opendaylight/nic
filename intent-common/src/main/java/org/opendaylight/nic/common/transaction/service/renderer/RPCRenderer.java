/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.renderer;

import com.google.common.collect.Lists;
import org.opendaylight.nic.common.transaction.exception.RemoveDataflowException;
import org.opendaylight.nic.common.transaction.exception.RendererServiceException;
import org.opendaylight.nic.common.transaction.utils.CommonUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.evpn.rev170724.intent.evpns.IntentEvpn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api._switch._interface.status.rev170811._switch.interfaces.status.SwitchInterfaceStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.queue.rev170807.evpn.dataflow.queue.EvpnDataflows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.queue.rev170807.evpn.dataflow.queues.EvpnDataflowQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.queue.rev170807.evpn.dataflow.queues.EvpnDataflowQueueBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by yrineu on 24/07/17.
 */
public class RPCRenderer implements RendererService {
    private static final Logger LOG = LoggerFactory.getLogger(RPCRenderer.class);

    private CommonUtils commonUtils;
    private RPCRendererUtils rpcRendererUtils;

    public RPCRenderer(final CommonUtils commonUtils) {
        this.commonUtils = commonUtils;
        this.rpcRendererUtils = new RPCRendererUtils(commonUtils);
    }

    //TODO: Make a refactoring
    @Override
    public void evaluateAction(String id) throws RendererServiceException {
        final IntentEvpn intentEvpn = commonUtils.retrieveIntentVlans(id);

        final String intentName = intentEvpn.getIntentEvpnName();
        final EvpnDataflowQueueBuilder queueBuilder = new EvpnDataflowQueueBuilder();

        final List<String> targetVlans = Lists.newArrayList();
        queueBuilder.setId(intentName);
        queueBuilder.setEvpnDataflows(Lists.newArrayList());
        intentEvpn.getEvpnServices().forEach(evpnService -> targetVlans.add(evpnService.getVlanName()));

        final List<EvpnDataflows> evpnDataflows = rpcRendererUtils.extractEvpnDataflows(targetVlans);
        LOG.info("\n### EvpnDataflows: {}", evpnDataflows.toString());
        if (!evpnDataflows.isEmpty()) {
            queueBuilder.getEvpnDataflows().addAll(evpnDataflows);
        }

//        final List<SwitchInterfaceStatus> switchInterfaceStatusList = rpcRendererUtils
//                .extractSwitchInterfaceStatus(evpnDataflows);

//        final ServiceMapping serviceMapping = createServiceMapping(evpnService.getServiceName(), targetVlan, hostInfo);
//        if (serviceMapping != null) {
//            commonUtils.pushServiceMapping(serviceMapping);
//        }
//        queueBuilder.setEvpnDataflowList(evpnDataflows);
//        if (!switchInterfaceStatusList.isEmpty()) {
//            commonUtils.pushSwitchInterfaceStatus(switchInterfaceStatusList, false);
//        }
        commonUtils.pushEvpnDataflowQueue(queueBuilder.build());
    }

    @Override
    public void evaluateRollBack(String id) throws RendererServiceException {
        try {
//            final EvpnDataflowQueue evpnDataflowQueue = commonUtils.retrieveEvpnDataflowQueue(id);
//            if (evpnDataflowQueue != null) {
//                final List<SwitchInterfaceStatus> interfaceStatus =
//                        rpcRendererUtils.extractSwitchInterfaceStatus(evpnDataflowQueue.getEvpnDataflows());
//                commonUtils.pushSwitchInterfaceStatus(interfaceStatus, true);
//            }
            commonUtils.removeEvpnDataFlow(id);
        } catch (RemoveDataflowException e) {
            throw new RendererServiceException(e.getMessage());
        }
    }

    @Override
    public void stopSchedule(String id) {
        //DO_NOTHING
    }

    @Override
    public void execute(DataObject dataflow) {

    }
}
