/*
 * Copyright (c) 2017 Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.impl;

import org.opendaylight.nic.of.renderer.api.MeterQueueService;
import org.opendaylight.nic.of.renderer.exception.MeterCreationExeption;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.idmanager.rev160406.AllocateIdInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.idmanager.rev160406.AllocateIdOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.idmanager.rev160406.IdManagerService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.idmanager.rev160406.ReleaseIdInputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MeterQueueServiceImpl implements MeterQueueService {

    private static final Logger LOG = LoggerFactory.getLogger(MeterQueueServiceImpl.class);

    private IdManagerService idManagerService;

    private static final String METER_ID_POOL_NAME = "METERS";

    public MeterQueueServiceImpl(final IdManagerService idManagerService) {
        this.idManagerService = idManagerService;
    }

    @Override
    public void start() {
        //TODO: Implement a startup action for Meters
    }

    @Override
    public long getNextMeterId(final String dataflowId) throws MeterCreationExeption {
        AllocateIdInputBuilder allocateBuilder = new AllocateIdInputBuilder();
        allocateBuilder.setPoolName(METER_ID_POOL_NAME.toLowerCase());
        allocateBuilder.setIdKey(dataflowId);
        Future<RpcResult<AllocateIdOutput>> allocateResult = idManagerService.allocateId(allocateBuilder.build());
        AllocateIdOutput allocatedId = null;
        try {
            RpcResult<AllocateIdOutput> allocate = allocateResult.get(5, TimeUnit.SECONDS);
            if (allocate.isSuccessful()) {
                allocatedId = allocate.getResult();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error(e.getMessage());
        }
        if (null == allocatedId) throw new MeterCreationExeption();
        return allocatedId.getIdValue();
    }

    @Override
    public Future<RpcResult<Void>> releaseMeterId(final String id) {
        final ReleaseIdInputBuilder inputBuilder = new ReleaseIdInputBuilder();
        inputBuilder.setPoolName(METER_ID_POOL_NAME.toLowerCase());
        inputBuilder.setIdKey(id);
        Future<RpcResult<Void>> releaseResult = idManagerService.releaseId(inputBuilder.build());
        try {
            releaseResult.get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error(e.getMessage());
        }
        return releaseResult;
    }

    @Override
    public void stop() {
        //TODO: Implement functionality to release all Meter IDs
    }
}
