/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.lifecycle;

/**
 * Defines the Action register for a given Intent based on Intent ID
 */
public interface IntentActionRegister {

    /**
     * Register to listen actions
     * @param id the {@link org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid}
     *           of a given Intent as String
     * @param listener the {@link IntentActionListener}
     */
    void register(String id, IntentActionListener listener);

    /**
     * Unsubscribe
     * @param listener the {@link IntentActionListener}
     */
    void unregister(IntentActionListener listener);
}
