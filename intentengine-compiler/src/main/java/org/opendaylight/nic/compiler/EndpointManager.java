//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.compiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.opendaylight.nic.compiler.api.Endpoint;

public class EndpointManager {

    private static Map<Epg, Set<Endpoint>> endpointMap = new HashMap<Epg, Set<Endpoint>>();

    public void addEndpoints(Epg epg, Set<Endpoint> endpoints) {

        endpointMap.put(epg, endpoints);
    }

}