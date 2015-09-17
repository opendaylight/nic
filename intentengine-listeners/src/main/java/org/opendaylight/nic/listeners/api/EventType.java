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
    LINKDISCOVERED,
    /** Indicates an existing link is overutilized. */
    LINKOVERUTILIZED,
    /** Indicates an existing link is removed. */
    LINKREMOVED,
    /** Indicates normal link utilization. */
    LINKUTILIZATIONNORMAL,
    /** Indicates new node is up. */
    NODEUPDATED,
    /** Indicates existing node is removed. */
    NODEREMOVED
}
