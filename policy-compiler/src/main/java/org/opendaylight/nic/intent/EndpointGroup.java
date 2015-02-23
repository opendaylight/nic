//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.intent;



/**
 * EndpointGroup is an attribute-based logical expression that can be evaluated to
 * create a set of endpoints.
 *
 * @author Shaun Wackerly
 * @author Duane Mentze
 */

public interface EndpointGroup {

    /**
     * The string-based logical expression which is a combination of attributes.
     *
     * @return string-based logical expression
     */
    public String group();

}