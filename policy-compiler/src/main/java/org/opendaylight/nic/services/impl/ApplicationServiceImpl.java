//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------
package org.opendaylight.nic.services.impl;

import java.util.HashMap;

import org.opendaylight.nic.common.AppId;
import org.opendaylight.nic.common.Application;
import org.opendaylight.nic.common.ApplicationImpl;
import org.opendaylight.nic.services.ApplicationService;


public class ApplicationServiceImpl implements ApplicationService {

    /** added Applications */
    private final HashMap<AppId,Application> apps;

    public ApplicationServiceImpl() {
        apps = new HashMap<>();
    }

    @Override
    public void add(Application app) {
        if (apps.containsKey(app.appId())) {
            throw new IllegalArgumentException("App already added: " + app.appId());
        }

        apps.put(app.appId(), app);
    }

    @Override
    public Application get(AppId app) {
        return apps.get(app);
    }

    @Override
    public void remove(AppId app) {
        apps.remove(app);
    }

}
