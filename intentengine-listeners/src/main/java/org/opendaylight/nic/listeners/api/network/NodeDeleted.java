/*
 * Copyright (c) 2015 Hewlett-Packard Development Company and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.nic.listeners.api.network;

import org.opendaylight.nic.listeners.api.NicNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;

public interface NodeDeleted extends NicNotification {

    NodeRef getNodeRef();
}
