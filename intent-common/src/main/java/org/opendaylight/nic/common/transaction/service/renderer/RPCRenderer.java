/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.renderer;

import com.google.common.collect.Lists;
import org.opendaylight.nic.common.transaction.exception.RendererServiceException;
import org.opendaylight.nic.common.transaction.utils.CommonUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.evpn.rev170724.intent.evpns.IntentEvpn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711._switch.infos.SwitchInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host._interface.rev170731.host._interface.Bridges;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.host.info.rev170724.HostInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflow.VlanInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflow.VlanInfosBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflows.EvpnDataflowBuilder;
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

    protected RPCRenderer(final CommonUtils commonUtils) {
        this.commonUtils = commonUtils;
    }

    @Override
    public void evaluateAction(String id) throws RendererServiceException {
        final IntentEvpn intentEvpn = commonUtils.retrieveIntentVlans(id);
        final HostInfos hostInfos = commonUtils.retrieveHostInfos();

        final String intentName = intentEvpn.getIntentEvpnName();

        intentEvpn.getEvpnServices().forEach(evpnService -> {
            final String targetVlan = evpnService.getVlanName();
            hostInfos.getHostInfo().forEach(hostInfo -> {
                final String switchName = hostInfo.getSwitchName().getValue();
                final SwitchInfo switchInfo = commonUtils.retrieveSwitchInfo(switchName);
                LOG.info("\n### SwitchInfo retrieved: {}", switchInfo.getModel());
                final EvpnDataflowBuilder evpnDataflowBuilder = new EvpnDataflowBuilder();
                evpnDataflowBuilder.setId(intentName);
                evpnDataflowBuilder.setHttpIp(switchInfo.getHttpIp());
                evpnDataflowBuilder.setHttpPort(switchInfo.getHttpPort());
                evpnDataflowBuilder.setUserName(switchInfo.getHttpUser());
                evpnDataflowBuilder.setPassword(switchInfo.getHttpPassword());
                evpnDataflowBuilder.setLoopbackIp(switchInfo.getLoopbackIp());
                hostInfo.getHostInterfaces().forEach(hostInterface -> {
                    final String switchInterface = hostInterface.getSwitchInterface();
                    final List<VlanInfos> vlanInfosList = createVlanInfosList(hostInterface.getBridges(), targetVlan);
                    evpnDataflowBuilder.setVlanInfos(vlanInfosList);
                    evpnDataflowBuilder.setInterfaceName(switchInterface);
                });
                commonUtils.pushEvpnDataflow(evpnDataflowBuilder.build());
            });
        });

}

    private List<VlanInfos> createVlanInfosList(final List<Bridges> bridgesList,
                                                final String tagertVlan) {
        final List<VlanInfos> vlanInfosList = Lists.newArrayList();
        final VlanInfosBuilder vlanInfosBuilder = new VlanInfosBuilder();
        bridgesList.forEach(bridge -> {
            final String vlanName = bridge.getVlanName().getValue();
            if (tagertVlan.equals(vlanName)) {
                vlanInfosBuilder.setVlanName(bridge.getVlanName().getValue());
                vlanInfosBuilder.setVlanId(bridge.getVlanId().longValue());
                vlanInfosList.add(vlanInfosBuilder.build());
            }
        });
        return vlanInfosList;
    }

    @Override
    public void evaluateRollBack(String id) throws RendererServiceException {
        //TODO: Implement rollback
    }

    @Override
    public void stopSchedule(String id) {

    }

    @Override
    public void execute(DataObject dataflow) {

    }
}
