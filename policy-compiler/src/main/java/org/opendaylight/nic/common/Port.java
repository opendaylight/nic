//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.common;

/**
 * An interface or port on a {@link Device}.
 *
 * @author Shaun Wackerly
 */
public interface Port {

    /**
     * Returns the numeric value associated with this port.
     *
     * @return numeric port value
     */
	Integer value();

}
