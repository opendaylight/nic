//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.services;

import java.util.Map;

import org.opendaylight.nic.common.AppId;
import org.opendaylight.nic.intent.Endpoint;
import org.opendaylight.nic.intent.EndpointAttribute;
import org.opendaylight.nic.intent.EndpointId;

/**
 * Provides a mechanism for management of endpoints and their attributes.
 * Applications may register {@link EndpointAttribute}s, apply those
 * attributes to specific endpoints, or add/remove endpoints from this
 * datastore.
 *
 * @author Shaun Wackerly
 * @authro Duane Mentze
 */
public interface EndpointService {

    /**
     * Registers the given endpoint attribute for use by the given application.
     * All endpoint attributes must be registered prior to applying them
     * to specific endpoints.
     *
     * @param attribute the endpoint attribute to register
     */
    void register(EndpointAttribute attribute, AppId app);

    /**
     * Apply the given endpoint attribute as being valid for the given endpoint.
     * Note that prior to use, all endpoint attributes must be registered by
     * the application intending to use them.
     *
     * @param attribute the endpoint attribute to apply
     * @param ep the endpoint to which the attribute will be applied
     */
    void apply(EndpointAttribute attribute, EndpointId ep);

    /**
     * Includes the endpoint in the policy domain
     *
     * @param endpoint to add
     */
    void add(Endpoint endpoint);

    /**
     * Removes the endpoint from the policy domain
     *
     * @param endpoint to remove
     */
    void remove(Endpoint endpoint);

    /**
     * Gets the specific endpoint within the policy domain.
     *
     * @param id endpoint ID
     * @return endpoint, or null if not found
     */
    Endpoint get(EndpointId id);

    /**
     * Gets all endpoints within the policy domain, mapped by ID.
     *
     * @return all endpoints
     */
    Map<EndpointId,Endpoint> getAll();

}
