/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.limiter.rev170310.intents.limiter.IntentLimiter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.intent.rev150122.intents.Intent;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;

public interface IntentCommonService {

    /**
     * Init all Intent processes
     */
    void start();

    /**
     * Extract and apply Intents
     * @param intent
     */
    void resolveAndApply(final Intent intent);

    /**
     * Extract and apply IntentLimiter. That Intent is used to
     * create Intents with meters
     * @param intent as an {@link Object}
     */
    void resolveAndApply(final Object intent);

    /**
     * Apply Intents for a given NodeId
     * @param nodeId
     */
    void resolveAndApply(final NodeId nodeId);

    /**
     * Extract and remove Intent limiter
     * @param intentLimiter
     */
    void resolveAndRemove(final IntentLimiter intentLimiter);

    /**
     * Extract and remove Intent
     * @param intent
     */
    void resolveAndRemove(final Intent intent);

    /**
     * Create ARP flows for a given NodeId
     * @param nodeId
     */
    void createARPFlow(final NodeId nodeId);

    /**
     * Create LLDP flows for a given NodeId
     * @param nodeId
     */
    void createLLDPFlow(final NodeId nodeId);

    /**
     * Stop all Intent processes
     */
    void stop();
}
