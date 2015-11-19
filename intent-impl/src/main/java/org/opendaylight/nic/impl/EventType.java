/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.impl;

public enum EventType {

    NODE_UP(1),
    NODE_UPDATED(2),
    NODE_DOWN(3),
    SWITCH_UP(1),
    SWITCH_DOWN(2),
    INTENT_CREATED(1),
    INTENT_UPDATED(2),
    INTENT_REMOVED(3);

    private int priority;
    private EventType(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

}
