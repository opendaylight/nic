//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent;

import org.opendaylight.nic.services.EndpointService;

/**
 * An attribute which may be applied to an {@link Endpoint}. The meaning of the
 * attribute is defined by the application which registers the attribute with
 * the {@link EndpointService}.
 *
 * @author Shaun Wackerly
 */
public interface EndpointAttribute {

    /**
     * Returns the name of this attribute.
     *
     * @return attribute name
     */
    String name();

}
