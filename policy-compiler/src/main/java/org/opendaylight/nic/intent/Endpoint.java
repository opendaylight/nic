//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent;

import java.util.Set;

/**
 * An endpoint on the network. An endpoint is a logically unique host on the
 * network which operates with its own identity. That identity is represented in
 * the controller by an {@link EndpointId}. An endpoint may have one or more
 * {@link EndpointAttribute}s applied to it, which indicate characteristics of
 * the endpoint within the controller's view.
 *
 * @author Shaun Wackerly
 */
public interface Endpoint {

    /**
     * Returns the identifier for this endpoint.
     *
     * @return unique ID
     */
    EndpointId id();

    /**
     * Returns the set of attributes which apply to this endpoint.
     *
     * @return set of attributes
     */
    Set<EndpointAttribute> attributes();

}
