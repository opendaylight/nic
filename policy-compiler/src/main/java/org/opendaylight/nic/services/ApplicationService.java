//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.services;

import org.opendaylight.nic.common.AppId;
import org.opendaylight.nic.common.Application;

/**
 * Provides a mechanism for management of applications. Applications register
 * with the controller to obtain an {@link AppId} which is used to uniquely
 * identify that application in other requests.
 *
 * @author Duane Mentze
 */
public interface ApplicationService {

    /**
     * Adds the given application as available for use.
     *
     * @param application
     *            to be added
     */
    void add(Application app);

    /**
     * Gets the application which corresponds to the given ID.
     *
     * @param id
     *            application ID
     * @return application, or null if not found
     */
    Application get(AppId app);

    /**
     * The given application is removed and no longer available for use.
     *
     * @param application
     *            to be removed
     */
    void remove(AppId app);
}
