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
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host.info.rev170724.HostInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.service.mapping.rev170801.ServiceMappings;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.service.mapping.rev170801.ServiceMappingsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.service.mapping.rev170801.service.mappings.ServiceMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.service.mapping.rev170801.service.mappings.ServiceMappingBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.queue.rev170807.evpn.dataflow.queue.EvpnDataflows;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.queue.rev170807.evpn.dataflow.queues.EvpnDataflowQueueBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

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

        final List<ServiceMapping> serviceMappings = Lists.newArrayList();
        final HostInfos hostInfos = commonUtils.retrieveHostInfos();
        intentEvpn.getEvpnServices().forEach(service -> hostInfos.getHostInfo().forEach(hostInfo ->
                hostInfo.getHostInterfaces().forEach(hostInterf ->
                        hostInterf.getBridges().forEach(bridges -> {
                            final String targetVlan = service.getVlanName();
                            final String hostVlan = bridges.getVlanName().getValue();

                            if (targetVlan.equals(hostVlan)) {
                                final String serviceName = service.getServiceName();
                                final String hostName = hostInfo.getHostName().getValue();
                                final ServiceMappingBuilder builder = new ServiceMappingBuilder();
                                builder.setId(serviceName + "_" + hostName);
                                builder.setHostName(hostName);
                                builder.setServiceName(service.getServiceName());
                                builder.setGatewayIp(hostInfo.getGatewayIp());
                                builder.setSubnetMask(hostInfo.getSubnetMask().getIpv4Prefix().getValue());
                                builder.setHostInterface(hostInterf.getInterfaceName().getValue());
                                builder.setHostBridge(bridges.getBridgeName().getValue());
                                builder.setVlanId(bridges.getVlanId().longValue());
                                builder.setHostIp(hostInfo.getIpAddress());
                                serviceMappings.add(builder.build());
                            }
                        }))));
        ServiceMappings mappings = commonUtils.retrieveServiceMappings();
        if (mappings == null) {
            mappings = new ServiceMappingsBuilder().setServiceMapping(Lists.newArrayList()).build();
        }
        mappings.getServiceMapping().addAll(serviceMappings);

        commonUtils.pushServiceMapping(mappings);
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
