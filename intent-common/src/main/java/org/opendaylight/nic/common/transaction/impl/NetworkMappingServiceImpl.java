/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.nic.common.transaction.api.NetworkMappingService;
import org.opendaylight.nic.common.transaction.utils.CommonUtils;
import org.opendaylight.protocol.util.Ipv4Util;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.pod.info.rev700101.pod.infos.PodInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.service.mapping.rev170801.ServiceMappings;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.service.mapping.rev170801.service.mappings.ServiceMapping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.network.mapping.service.mapping.rev170801.service.mappings.ServiceMappingBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yrineu on 14/09/17.
 */
public class NetworkMappingServiceImpl implements NetworkMappingService {
    public static final Logger LOG = LoggerFactory.getLogger(NetworkMappingServiceImpl.class);

    private final CommonUtils commonUtils;

    public NetworkMappingServiceImpl(final DataBroker dataBroker) {
        this.commonUtils = new CommonUtils(dataBroker);
    }

    @Override
    public void processNetworkMappingChange(PodInfo podInfo) {
        //TODO: Integrate with Genius IP management
        final String podId = podInfo.getId();
        LOG.info("\n### Processing new POD IP for POD with ID: {}", podId);
        final ServiceMapping serviceMapping = commonUtils.retrieveServiceMappingById(podId);
        final IpAddress podIp = serviceMapping.getPodip();

        LOG.info("\n### OLD IP: {}", podIp.getIpv4Address().getValue());
        final Ipv4Address newIp = Ipv4Util.incrementIpv4Address(podIp.getIpv4Address());
        LOG.info("\n### New IP: {}", newIp.getValue());
        final ServiceMappingBuilder mappingBuilder = new ServiceMappingBuilder(serviceMapping);
        mappingBuilder.setPodip(new IpAddress(newIp));

        final ServiceMappings serviceMappings = commonUtils.retrieveServiceMappings();
        serviceMappings.getServiceMapping().remove(serviceMapping);
        serviceMappings.getServiceMapping().add(mappingBuilder.build());

        commonUtils.pushServiceMapping(serviceMappings);
        commonUtils.removePodInfo(podId);
    }
}
