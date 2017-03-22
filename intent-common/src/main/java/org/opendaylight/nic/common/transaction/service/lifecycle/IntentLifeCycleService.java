/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.lifecycle;

import org.opendaylight.nic.utils.EventType;

/**
 * The life cycle service for a given Intent
 */
public interface IntentLifeCycleService {

    /**
     * Start the life cycle management process to a given Intent
     * @param id the {@link org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid}
     *           of a given Intent as String
     * @param eventType the {@link EventType}
     */
    void startTransaction(String id, EventType eventType);
}
