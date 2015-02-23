//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------

package org.opendaylight.nic.common;

import org.opendaylight.nic.intent.Policy;

/**
 * Applications control the network behavior by setting {@link Policy}s.
 *
 * @author Duane Mentze
 */
public interface Application {

    public String name();

    public AppId appId();

    public long priority();

    // TODO this is a placeholder for permissions.
    String permissions();

}
