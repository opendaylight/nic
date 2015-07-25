//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.opendaylight.nic.compiler.EndpointImpl;
import org.opendaylight.nic.compiler.Epg;
import org.opendaylight.nic.compiler.api.Endpoint;

public class EndpointManager {

    private static Map<Epg, Set<Endpoint>> endpointMap =
            new HashMap<Epg, Set<Endpoint>>();

    public EndpointManager() throws Exception {

        endpointMap.put(new Epg("Finance"),
                endpoints("192.168.1.1", "192.168.1.2", "192.168.1.3"));

        endpointMap.put(new Epg("Marketing"),
                endpoints("192.168.1.4", "192.168.1.5", "192.168.1.6"));

        endpointMap.put(new Epg("Infected"),
                endpoints("192.168.1.1", "192.168.1.7"));
    }

    public Map<Epg, Set<Endpoint>> getEndpointMap() {
        return endpointMap;
    }

    private Set<Endpoint> endpoints(String... hosts)
            throws UnknownHostException {
        Set<Endpoint> endpoints = new LinkedHashSet<>();
        for (String host : hosts) {
            Endpoint endpoint = new EndpointImpl(InetAddress.getByName(host));
            endpoints.add(endpoint);
        }
        return endpoints;

    }
}