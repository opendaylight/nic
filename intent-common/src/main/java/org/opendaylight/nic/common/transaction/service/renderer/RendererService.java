/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.renderer;

import org.opendaylight.nic.common.transaction.exception.RendererServiceException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.renderer.api.dataflow.rev170309.dataflows.Dataflow;

/**
 * Abstract renderer service
 */
public interface RendererService {

    /**
     * Evaluate an action for Intent
     * @param id the {@link org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.IntentLimiter} ID
     * @throws RendererServiceException in case of failure when try to apply that Intent
     */
    void evaluateAction(String id) throws RendererServiceException;

    /**
     * Evaluate rollback in case of failure
     * @param id the {@link org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.IntentLimiter} ID
     * @throws RendererServiceException
     */
    void evaluateRollBack(String id) throws RendererServiceException;

    /**
     * Stop the scheduler for a given Intent
     * @param id the {@link org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.IntentLimiter} ID
     */
    void stopSchedule(String id);

    /**
     * Execute a given {@link Dataflow} extracted from a given
     * {@link org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.IntentLimiter}
     * @param dataflow the {@link Dataflow}
     */
    void execute(Dataflow dataflow);
}
