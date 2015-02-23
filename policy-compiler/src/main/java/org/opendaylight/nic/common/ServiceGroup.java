//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.common;

/**
 * A special set of endpoints that are used in servicing actions that require an
 * endpoint. For example, AUDIT specifies the server where a copy of the packets
 * are sent, INSPECT specifies the servers where the inspection will occur, and
 * REDIRECT specifies servers to which the traffic should be redirected.
 *
 * @author Shaun Wackerly
 */
public interface ServiceGroup {
    Port port();

}
