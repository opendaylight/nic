/*
 * Copyright (c) 2017 Serro LCC. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.common.transaction.service.lifecycle;

/**
 * Defines the action listener for a given Intent ID
 */
public interface IntentActionListener {

    /**
     * Proceed to the next action of a given event
     * @param id the {@link org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid}
     *           of a given Intent as String
     */
    void proceedToNext(String id);

    /**
     * Proceed to the next action when some fail happens
     * @param id the {@link org.opendaylight.yang.gen.v1.urn.opendaylight.intent.types.rev150122.Uuid}
     *           of a given Intent as String
     */
    void proceedToNextFailed(String id);
}
