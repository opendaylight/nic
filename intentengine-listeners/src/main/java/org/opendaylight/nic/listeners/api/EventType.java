/*
 * Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.api;

public enum EventType {
    /** Indicates a new link is discovered. */
    LINK_DISCOVERED,
    /** Indicates an existing link is overutilized. */
    LINK_OVERUTILIZED,
    /** Indicates an existing link is removed. */
    LINK_REMOVED,
    /** Indicates normal link utilization. */
    LINK_UTILIZATION_NORMAL,
    /** Indicates new node is up. */
    NODE_UPDATED,
    /** Indicates existing node is removed. */
    NODE_REMOVED,
    /** Indicates new intent is up */
    INTENT_ADDED,
    /** Indicates existing intent is removed */
    INTENT_REMOVED
}
