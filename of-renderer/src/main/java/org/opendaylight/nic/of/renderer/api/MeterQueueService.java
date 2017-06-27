/*
 * Copyright (c) 2017 Serro LLC.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.of.renderer.api;

import org.opendaylight.nic.of.renderer.exception.MeterCreationExeption;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.util.concurrent.Future;

/**
 * Service used to allocate or release a {@link org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId}
 */
public interface MeterQueueService {

    /**
     * Start Meter Queue services
     */
    void start();

    /**
     * Get the next {@link org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId} value
     * using the Genius's {@link org.opendaylight.yang.gen.v1.urn.opendaylight.genius.idmanager.rev160406.IdManagerService}
     * @param dataflowId
     * @return an ID as {@link Long}
     * @throws MeterCreationExeption
     */
    long getNextMeterId(String dataflowId) throws MeterCreationExeption;

    /**
     * Release the value for a given key, the key is defined by the
     * {@link org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.Dataflow}
     * ID as {@link String}
     * @param id
     * @return the {@link Future} as response
     */
    Future<RpcResult<Void>> releaseMeterId(String id);

    /**
     * Stop Meter Queue services
     */
    void stop();
}
