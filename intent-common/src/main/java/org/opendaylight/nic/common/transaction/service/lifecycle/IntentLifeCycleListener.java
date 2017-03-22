/*
 * Copyright (c) 2017 Serro LLC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.lifecycle;

/**
 * Defines the Intent life cycle listener
 */
public interface IntentLifeCycleListener {

    /**
     * Notify when the transaction for a given Intent was started
     */
    void transactionStarted();

    /**
     * Notify to proceed with execution for a given Intent
     */
    void proceedExecution();

    /**
     * Notify to stop the execution for a given Intent
     */
    void stopExecution();
}
