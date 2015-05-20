//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler.api;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Set;

public interface IntentCompiler {
    Collection<Policy> compile(Collection<Policy> policies);
    Set<Endpoint> parseEndpointGroup(String csv) throws UnknownHostException;
    Policy createPolicy(Set<Endpoint> source, Set<Endpoint> destination, Action action);
}
