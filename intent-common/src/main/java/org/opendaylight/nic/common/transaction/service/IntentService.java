/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service;

/**
 * This interface defines the service for ar given Intent
 */
public interface IntentService {

    /**
     * Start the life cycle management for a given Intent using the ID
     * @param id the {@link org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid}
     *           of a given Intent as String
     */
    void start(String id);

    /**
     * Clear all intent action listener list
     */
    void clearActionListerList();
}
