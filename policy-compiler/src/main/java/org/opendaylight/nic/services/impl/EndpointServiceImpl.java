//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.services.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opendaylight.nic.common.AppId;
import org.opendaylight.nic.common.Application;
import org.opendaylight.nic.intent.Endpoint;
import org.opendaylight.nic.intent.EndpointAttribute;
import org.opendaylight.nic.intent.EndpointId;
import org.opendaylight.nic.services.ApplicationService;
import org.opendaylight.nic.services.EndpointService;

public class EndpointServiceImpl implements EndpointService {

    /** added endpoints */
    private final Map<EndpointId, Endpoint> endpoints;
    /** registered attributes */
    private final Map<AppId, Set<EndpointAttribute>> attributes;
    private final ApplicationService as;

    public Map<EndpointId, Endpoint> getEndpoints() {
        return endpoints;
    }

    public EndpointServiceImpl(ApplicationService as) {
        this.as = as;
        endpoints = new HashMap<>();
        attributes = new HashMap<>();
    }

    @Override
    public void register(EndpointAttribute attribute, AppId id) {
        Application app = as.get(id);
        if (app == null) {
            throw new IllegalStateException("App not registered: " + id);
        }

        if (attributes.get(id) == null) {
            attributes.put(id, new HashSet<EndpointAttribute>());
        } else {
            if (attributes.get(id).contains(attribute)) {
                throw new IllegalArgumentException(
                        "EndpointAttribute already added: " + attribute);
            }
        }
        attributes.get(id).add(attribute);
    }

    @Override
    public void apply(EndpointAttribute attribute, EndpointId ep) {

        if (!endpoints.containsKey(ep)) {
            throw new IllegalStateException("Endpoint not added: " + ep);
        }

        Endpoint e = endpoints.get(ep);
        if (e.attributes().contains(attribute)) {
            throw new IllegalArgumentException(
                    "EndpointAttribute already added: " + attribute);
        }

        e.attributes().add(attribute);
    }

    @Override
    public void add(Endpoint endpoint) {
        if (endpoints.containsKey(endpoint.id())) {
            throw new IllegalArgumentException("Endpoint already added: "
                    + endpoint);
        }
        endpoints.put(endpoint.id(), endpoint);
    }

    @Override
    public void remove(Endpoint endpoint) {
        endpoints.remove(endpoint.id());
    }

    @Override
    public Endpoint get(EndpointId id) {
        return endpoints.get(id);
    }

    @Override
    public Map<EndpointId, Endpoint> getAll() {
        return Collections.unmodifiableMap(endpoints);
    }

}
