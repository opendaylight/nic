/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.opendaylight.nic.common.transaction.exception.RendererServiceException;
import org.opendaylight.nic.common.transaction.utils.CommonUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.vlan.rev170724.intent.vlans.IntentVlan;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.group.rev170724._switch.group.HostByInterfaceList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.group.rev170724._switch.groups.SwitchGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping._switch.info.rev170711._switch.infos.SwitchInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.vlan.group.rev170724.vlan.groups.VlanGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflow.VlanInfos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflow.VlanInfosBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.evpn.dataflow.rev170724.evpn.dataflows.EvpnDataflowBuilder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
        final IntentVlan intentVlan = commonUtils.retrieveIntentVlans(id);

        final String intentName = intentVlan.getName();
        final String hostName = intentVlan.getHostName();
        final String vlanGroupName = intentVlan.getVlanGroup();
        LOG.info("\n### Host name: {}", hostName);
        LOG.info("\n### VLANGroupName: {}", vlanGroupName);

        final VlanGroup vlanGroup = commonUtils.retrieveVlanGroup(vlanGroupName);
        final String switchGroupName = vlanGroup.getSwitchName().getValue();
        LOG.info("\n### SwitchGroupName: {}", switchGroupName);

        final Set<String> vlanNames = Sets.newHashSet();

        vlanGroup.getVlanNames().forEach(vlanName -> vlanNames.add(vlanName.getVlanName().getValue()));
        LOG.info("\n### VLANNames {}", vlanNames.toString());

        final Map<String, Integer> vlanNameById = commonUtils.retrieveVlanNameById(vlanNames);

        final SwitchGroup switchGroup = commonUtils.retrieveSwitchGroup(switchGroupName);
        final String switchName = switchGroup.getSwitchName().getValue();

        final Set<String> interfaceName = Sets.newHashSet();
        final Iterator<HostByInterfaceList> iterator = switchGroup.getHostByInterfaceList().iterator();
        while (iterator.hasNext()) {
            final HostByInterfaceList hostByInterface = iterator.next();
            if (hostByInterface.getHostName().getValue().equals(hostName)) {
                interfaceName.add(hostByInterface.getInterfaceName());
            }
        }

        final SwitchInfo switchInfo = commonUtils.retrieveSwitchInfo(switchName);
        LOG.info("\n### SwitchInfo retrieved: {}", switchInfo.getModel());
        final EvpnDataflowBuilder evpnDataflowBuilder = new EvpnDataflowBuilder();
        evpnDataflowBuilder.setId(intentName);
        evpnDataflowBuilder.setHttpIp(switchInfo.getHttpIp());
        evpnDataflowBuilder.setHttpPort(switchInfo.getHttpPort());
        evpnDataflowBuilder.setUserName(switchInfo.getHttpUser());
        evpnDataflowBuilder.setPassword(switchInfo.getHttpPassword());
        evpnDataflowBuilder.setLoopbackIp(switchInfo.getLoopbackIp());

        final List<VlanInfos> vlanInfosList = Lists.newArrayList();
        final VlanInfosBuilder vlanInfosBuilder = new VlanInfosBuilder();
        vlanNameById.entrySet().forEach(entry -> {
            vlanInfosBuilder.setVlanName(entry.getKey());
            vlanInfosBuilder.setVlanId(entry.getValue().longValue());
            vlanInfosList.add(vlanInfosBuilder.build());
        });

        evpnDataflowBuilder.setVlanInfos(vlanInfosList);
        evpnDataflowBuilder.setInterfaceName(interfaceName.iterator().next());
        evpnDataflowBuilder.setOspfId(switchName);

        commonUtils.pushEvpnDataflow(evpnDataflowBuilder.build());
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
