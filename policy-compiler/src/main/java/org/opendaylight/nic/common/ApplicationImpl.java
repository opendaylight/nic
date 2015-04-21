//------------------------------------------------------------------------------
// Copyright (c) 2015 Hewlett-Packard Development Company, L.P. and others.  All rights reserved.
// 
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v1.0 which accompanies this distribution,
// and is available at http://www.eclipse.org/legal/epl-v10.html
//------------------------------------------------------------------------------

package org.opendaylight.nic.common;

import org.opendaylight.nic.intent.Policy;

/**
 * Applications control the network behavior by setting {@link Policy}s.
 *
 * @author Duane Mentze
 */
public class ApplicationImpl implements Application {

    String name;
    long priority;
    AppId appId;
    String permissions;

    @Override
    public String name() {
        return name;
    }

    @Override
    public long priority() {
        return priority;
    }

    @Override
    public org.opendaylight.nic.common.AppId appId() {
        return appId;
    }

    @Override
    public String permissions() {
        return permissions;
    }

    public ApplicationImpl(String name, long priority) {
        this.name = name;
        this.priority = priority;
        this.appId = new AppId(name);
        this.permissions = "";
    }

    public ApplicationImpl(ApplicationImpl app) {
        this.name = app.name;
        this.priority = app.priority;
        this.appId = new AppId(app.name);
        this.permissions = app.permissions;
    }

    public ApplicationImpl(Application app) {
        this.name = app.name();
        this.priority = app.priority();
        this.appId = new AppId(app.name());
        this.permissions = app.permissions();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appId == null) ? 0 : appId.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((permissions == null) ? 0 : permissions.hashCode());
        result = prime * result + (int) (priority ^ (priority >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ApplicationImpl other = (ApplicationImpl) obj;
        if (appId == null) {
            if (other.appId != null)
                return false;
        } else if (!appId.equals(other.appId))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (permissions == null) {
            if (other.permissions != null)
                return false;
        } else if (!permissions.equals(other.permissions))
            return false;
        if (priority != other.priority)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ApplicationImpl [name=" + name + ", priority=" + priority
                + ", appId=" + appId + ", permissions=" + permissions + "]";
    }

}
