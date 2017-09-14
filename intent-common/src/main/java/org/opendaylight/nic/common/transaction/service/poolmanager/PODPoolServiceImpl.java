/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.poolmanager;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.idmanager.rev160406.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.itm.config.rev160406.vtep.ip.pools.VtepIpPoolBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.itm.rev160406.transport.zones.transport.zone.subnets.VtepsBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by yrineu on 12/09/17.
 */
public class PODPoolServiceImpl implements PODPoolService {
    private static final Logger LOG = LoggerFactory.getLogger(PODPoolServiceImpl.class);

    private final static String POD_POOL_NAME = "POD_IP_POOL";
    private final DataBroker dataBroker;
    private final IdManagerService idManagerService;

    public PODPoolServiceImpl(final DataBroker dataBroker,
                              final IdManagerService idManagerService) {
        this.dataBroker = dataBroker;
        this.idManagerService = idManagerService;
    }

    @Override
    public void start() {
        final CreateIdPoolInputBuilder idPoolInputBuilder = new CreateIdPoolInputBuilder();
        final AllocateIdRangeInputBuilder rangeBuilder = new AllocateIdRangeInputBuilder();
        VtepIpPoolBuilder vtepIpPoolBuilder = new VtepIpPoolBuilder();

        idPoolInputBuilder.setPoolName(POD_POOL_NAME);
        idPoolInputBuilder.setLow(1L);
        idPoolInputBuilder.setHigh(254L);

        Future<RpcResult<Void>> poolCreateResult = idManagerService.createIdPool(idPoolInputBuilder.build());
        try {
            poolCreateResult.get(2, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            LOG.error(e.getMessage());
        }
        LOG.info("\nPODPoolService initialized with success.");
    }

    @Override
    public String getNextPODIp() {
        return null;
    }

    @Override
    public void lockPODIp(final String podIp) {
        final AllocateIdInputBuilder builder = new AllocateIdInputBuilder();
        builder.setPoolName(POD_POOL_NAME);
        builder.setIdKey(podIp);
        Future<RpcResult<AllocateIdOutput>> result = idManagerService.allocateId(builder.build());
        try {
            RpcResult<AllocateIdOutput> allocatedIp = result.get(5, TimeUnit.SECONDS);
            if (!allocatedIp.isSuccessful()) {
                //TODO: Throws an exception
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            //TODO: Throws an exception
        }

    }

    @Override
    public void releasePODIp(String podIP) {
        final ReleaseIdInputBuilder builder = new ReleaseIdInputBuilder();
        builder.setPoolName(POD_POOL_NAME);
        builder.setIdKey(podIP);
        Future<RpcResult<Void>> result = idManagerService.releaseId(builder.build());
    }

    @Override
    public void stop() {

    }
}
