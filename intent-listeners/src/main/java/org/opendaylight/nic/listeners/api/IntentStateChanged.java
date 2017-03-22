/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.listeners.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.nic.intent.state.transaction.rev151203.IntentStateTransactions;

/**
 * Event created when a given {@link IntentStateTransactions}
 * was created
 */
public interface IntentStateChanged extends NicNotification {

    /**
     * Retrieve the {@link IntentStateTransactions}
     * @return the {@link IntentStateTransactions}
     */
    IntentStateTransactions getIntentStateTransaction();
}
