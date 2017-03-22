/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.lifecycle;

/**
 * The service to listen about life cycle changes.
 */
public interface IntentLifeCycleRegister {

    /**
     * Register to listen about Life cycle changes
     * @param listener the {@link IntentLifeCycleListener}
     */
    void register(IntentLifeCycleListener listener);

    /**
     * Unsubscribe to listen about Intent life cycle changes
     * @param listener the {@link IntentLifeCycleListener}
     */
    void unregister(IntentLifeCycleListener listener);
}
