/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.listeners;

import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * This service is responsible to handle Intent events.
 */
public interface IntentTreeChangesListener<T extends DataObject> extends DataTreeChangeListener<T> {

    /**
     * Start intent listener services
     */
    void start();

    /**
     * Stop Intent listener services
     */
    void stop();
}
